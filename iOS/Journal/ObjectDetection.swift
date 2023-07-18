import UIKit
import Vision
import AVFoundation

class ObjectDetection {
  
  var synthesizer : AVSpeechSynthesizer!
  var objectDetectionLayer: CALayer!
  var lastRecognizedDictionary : [String: String] = [:]
  var canRecognizeObjects : Bool = false
  var numPlayedTimes : Int = 0
  
  weak var timer: Timer?
  
  init(_ viewLayer: CALayer, videoFrameSize: CGSize, synthesizer: AVSpeechSynthesizer) {
    self.synthesizer = synthesizer
    
    self.setupObjectDetectionLayer(viewLayer, videoFrameSize)
  }
  
  deinit {
    if(self.synthesizer.isSpeaking) {
      self.synthesizer.stopSpeaking(at: .immediate)
    }
    
    self.synthesizer = nil
    self.objectDetectionLayer = nil
  }
  
  public func createObjectDetectionVisionRequest() -> VNRequest? {
    let config = MLModelConfiguration()
        
    guard let model = try? yolov2_pipeline(configuration: config).model, let visionModel = try? VNCoreMLModel(for: model) else {
      print("Model loading error")
      
      return nil
    }

    let objectRecognition = VNCoreMLRequest(model: visionModel, completionHandler: { (request, error) in
      DispatchQueue.main.async(execute: {
        if let results = request.results {
          self.processVisionRequestResults(results)
        }
      })
    })

    objectRecognition.imageCropAndScaleOption = .scaleFill
    
    return objectRecognition
  }
  
  private func setupObjectDetectionLayer(_ viewLayer: CALayer, _ videoFrameSize: CGSize) {
    self.objectDetectionLayer = CALayer()
    self.objectDetectionLayer.name = "ObjectDetectionLayer"
    self.objectDetectionLayer.bounds = CGRect(x: 0.0, y: 0.0, width: videoFrameSize.width, height: videoFrameSize.height)
    self.objectDetectionLayer.position = CGPoint(x: viewLayer.bounds.midX, y: viewLayer.bounds.midY)
    
    viewLayer.addSublayer(self.objectDetectionLayer)

    let bounds = viewLayer.bounds
    let scale = fmax(bounds.size.width  / videoFrameSize.width, bounds.size.height / videoFrameSize.height)
    
    CATransaction.begin()
    CATransaction.setValue(kCFBooleanTrue, forKey: kCATransactionDisableActions)
    
    self.objectDetectionLayer.setAffineTransform(CGAffineTransform(scaleX: scale, y: -scale))
    self.objectDetectionLayer.position = CGPoint(x: bounds.midX, y: bounds.midY)
    
    CATransaction.commit()
  }

  private func createBoundingBoxLayer(_ bounds: CGRect, identifier: String, confidence: VNConfidence) -> CALayer {
    let path = UIBezierPath(rect: bounds)
    
    let boxLayer = CAShapeLayer()
    boxLayer.path = path.cgPath
    boxLayer.strokeColor = UIColor.red.cgColor
    boxLayer.lineWidth = 2
    boxLayer.fillColor = CGColor(colorSpace: CGColorSpaceCreateDeviceRGB(), components: [0.0, 0.0, 0.0, 0.0])
    
    boxLayer.bounds = bounds
    boxLayer.position = CGPoint(x: bounds.midX, y: bounds.midY)
    boxLayer.name = "Detected Object Box"
    boxLayer.backgroundColor = CGColor(colorSpace: CGColorSpaceCreateDeviceRGB(), components: [0.5, 0.5, 0.2, 0.5])
    boxLayer.cornerRadius = 6
    
    let textLayer = DynamicTextLayer()
    textLayer.alignmentMode = .center
    textLayer.fontSize = 40
    textLayer.string = NSLocalizedString(identifier, comment: identifier).capitalized
    textLayer.adjustsFontSizeToFitWidth = true
    
    textLayer.bounds = CGRect(x: 0, y: 0, width: bounds.size.width - 10, height: bounds.size.height - 10)
    textLayer.position = CGPoint(x: bounds.midX, y: bounds.midY)
    textLayer.alignmentMode = .center
    textLayer.foregroundColor =  UIColor.red.cgColor
    textLayer.contentsScale = 2.0
    
    textLayer.setAffineTransform(CGAffineTransform(scaleX: 1.0, y: -1.0))
    
    boxLayer.addSublayer(textLayer)
    
    return boxLayer
  }
  
  private func processVisionRequestResults(_ results: [Any]) {
    self.objectDetectionLayer.sublayers = nil
    
    CATransaction.begin()
    CATransaction.setValue(kCFBooleanTrue, forKey: kCATransactionDisableActions)
    
    var voiceOverText = ""
    var detectedDictionary : [String: String] = [:]
    
    for observation in results where observation is VNRecognizedObjectObservation {
      guard let objectObservation = observation as? VNRecognizedObjectObservation else {
        continue
      }
      
      let topLabelObservation = objectObservation.labels[0]
      
      if(topLabelObservation.confidence > 0.9) {
        let objectBounds = VNImageRectForNormalizedRect(objectObservation.boundingBox, Int(self.objectDetectionLayer.bounds.width), Int(self.objectDetectionLayer.bounds.height))
        
        let bbLayer = self.createBoundingBoxLayer(objectBounds, identifier: topLabelObservation.identifier, confidence: topLabelObservation.confidence)
        self.objectDetectionLayer.addSublayer(bbLayer)
        
        if(detectedDictionary[topLabelObservation.identifier] == nil && !synthesizer.isSpeaking) {
          detectedDictionary[topLabelObservation.identifier] = topLabelObservation.identifier
        }
      }
    }
    
    for detectedObjects in detectedDictionary {
      if(lastRecognizedDictionary[detectedObjects.key] == nil) {
        voiceOverText += NSLocalizedString(detectedObjects.key, comment: detectedObjects.key) + ", "
      }
    }
    
    if(voiceOverText != "" && self.canRecognizeObjects) {
      var audioSpeech = ""
      
      if(self.numPlayedTimes == 0) {
        audioSpeech = NSLocalizedString("audioSurrounding", comment: "audioSurrounding") + voiceOverText
      } else {
        audioSpeech = voiceOverText
      }
      
      let utterance = AVSpeechUtterance(string: audioSpeech)
      utterance.voice = AVSpeechSynthesisVoice(language: NSLocalizedString("audioLanguage", comment: "audioLanguage"))
      
      synthesizer.speak(utterance)
      lastRecognizedDictionary = detectedDictionary
      
      self.numPlayedTimes += 1
      self.resetTimer()
    }
    
    CATransaction.commit()
  }
  
  func resetTimer() {
      timer?.invalidate()
      timer = .scheduledTimer(withTimeInterval: 5.0, repeats: false) { [weak self] timer in
        self?.lastRecognizedDictionary = [:]
      }
  }
}

extension String {
  func size(OfFont font: UIFont) -> CGSize {
    return (self as NSString).size(withAttributes: [NSAttributedString.Key.font: font])
  }
}

class DynamicTextLayer : CATextLayer {
  var adjustsFontSizeToFitWidth = false

  override func layoutSublayers() {
    super.layoutSublayers()
    
    if adjustsFontSizeToFitWidth {
      fitToFrame()
    }
  }

  func fitToFrame(){
    var stringSize: CGSize  {
      get { return (string as? String)!.size(OfFont: UIFont(name: (font as! UIFont).fontName, size: fontSize)!) }
    }
    
    let inset: CGFloat = 2
    
    while frame.width < stringSize.width + inset {
      fontSize -= 1
    }
  }
}
