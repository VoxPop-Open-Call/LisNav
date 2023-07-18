import UIKit
import AVFoundation
import Vision

class VideoCapture: NSObject, AVCaptureVideoDataOutputSampleBufferDelegate {
    private let captureSession = AVCaptureSession()
    private var videoPreviewLayer: AVCaptureVideoPreviewLayer! = nil
    private let videoDataOutput = AVCaptureVideoDataOutput()
    private let videoDataOutputQueue = DispatchQueue(label: "VideoDataOutput", qos: .userInitiated, attributes: [], autoreleaseFrequency: .workItem)
    
    private var videoFrameSize: CGSize = .zero
    
    private var visionRequests =  [VNRequest]()
    
    init(_ viewLayer: CALayer) {
      super.init()
      self.setupPreview(viewLayer)
    }

    deinit {
      if (self.captureSession.isRunning) {
        self.captureSession.stopRunning()
      }
      
      self.videoPreviewLayer.removeFromSuperlayer()
      self.videoPreviewLayer = nil
    }
    
    private func setupPreview(_ viewLayer: CALayer) {
      var deviceInput: AVCaptureDeviceInput!
      
      let videoDevice = AVCaptureDevice.DiscoverySession(deviceTypes: [.builtInWideAngleCamera], mediaType: .video, position: .back).devices.first
      do {
        deviceInput = try AVCaptureDeviceInput(device: videoDevice!)
      } catch {
        print("Could not create video device input: \(error)")
        return
      }
      
      captureSession.beginConfiguration()
      captureSession.sessionPreset = .vga640x480

      self.videoFrameSize = CGSize(width: 480, height: 640)

      guard captureSession.canAddInput(deviceInput) else {
        print("Could not add video device input to the session")
        captureSession.commitConfiguration()
        return
      }
      
      captureSession.addInput(deviceInput)
      if captureSession.canAddOutput(videoDataOutput) {
        captureSession.addOutput(videoDataOutput)
        
        videoDataOutput.alwaysDiscardsLateVideoFrames = true
        videoDataOutput.videoSettings = [kCVPixelBufferPixelFormatTypeKey as String: Int(kCVPixelFormatType_420YpCbCr8BiPlanarFullRange)]
        videoDataOutput.setSampleBufferDelegate(self, queue: videoDataOutputQueue)
      } else {
        print("Could not add video data output to the session")
        captureSession.commitConfiguration()
        
        return
      }
      
      let captureConnection = videoDataOutput.connection(with: .video)
      captureConnection?.isEnabled = true
      captureConnection?.videoOrientation = .portrait

      captureSession.commitConfiguration()

      self.videoPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
      self.videoPreviewLayer.videoGravity = .resizeAspectFill
      
      videoPreviewLayer.frame = viewLayer.bounds
      viewLayer.addSublayer(videoPreviewLayer)
    }
    
    public func getCaptureFrameSize() -> CGSize {
      return self.videoFrameSize
    }
    
    public func startCapture(_ visionRequest: VNRequest?) {
      if visionRequest != nil {
          self.visionRequests = [visionRequest!]
      } else {
          self.visionRequests = []
      }

      DispatchQueue.global(qos: .background).async {
        if !self.captureSession.isRunning {
          self.captureSession.startRunning()
        }
      }
    }
  
    public func stopCapture() {
      if (self.captureSession.isRunning) {
        self.captureSession.stopRunning()
      }
      
      self.videoPreviewLayer.removeFromSuperlayer()
      self.videoPreviewLayer = nil
    }
    
    public func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
      guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else {
        return
      }

      let frameOrientation: CGImagePropertyOrientation = .up
      
      let imageRequestHandler = VNImageRequestHandler(cvPixelBuffer: pixelBuffer, orientation: frameOrientation, options: [:])
      
      do {
        try imageRequestHandler.perform(self.visionRequests)
      } catch {
        print(error)
      }
    }

    public func captureOutput(_ captureOutput: AVCaptureOutput, didDrop didDropSampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
    }
}
