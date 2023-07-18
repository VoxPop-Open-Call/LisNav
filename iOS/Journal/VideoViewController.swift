import UIKit
import AVFoundation
import Vision
import CoreLocation
import MapKit
import MediaPlayer

class VideoViewController: UIViewController, MKMapViewDelegate {
  @IBOutlet weak var mapView: MKMapView!
  @IBOutlet weak var locationbutton: UIButton!
  
  var annotations : Array = [Station]()
  var idsLastTTS : [Int] = []
  var isProcessing : Bool = false
  var actualUserLocation : MKUserLocation?
  var hasDescribedPosition : Bool = false
  
  var hasURLData : Bool = false
  
  let distanceThreshold : Double = 100
  let synthesizer = AVSpeechSynthesizer()
  
  @IBOutlet weak var cameraView: UIView!
  @IBOutlet weak var closeButton: UIButton!
  
  var videoCapture: VideoCapture!
  var objectDetection: ObjectDetection!
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    self.cameraView.bounds = UIScreen.main.bounds
    
    self.videoCapture = VideoCapture(self.cameraView.layer)
    self.objectDetection = ObjectDetection(self.cameraView.layer, videoFrameSize: self.videoCapture.getCaptureFrameSize(), synthesizer: self.synthesizer)
    
    let visionRequest = self.objectDetection.createObjectDetectionVisionRequest()
    self.videoCapture.startCapture(visionRequest)
    
    self.closeButton.setTitle(NSLocalizedString("close", comment: "close"), for: .normal)

    self.overrideUserInterfaceStyle = .light
    
    UIApplication.shared.isIdleTimerDisabled = true
    
    MPVolumeView.setVolume(1.0)
    
    if (try? AVAudioSession.sharedInstance().setCategory(.playback)) != nil {
      if (try? AVAudioSession.sharedInstance().setActive(true)) != nil {
        print("Ensuring silent mode is disabled")
      }
    }
    
    mapView.userTrackingMode = .followWithHeading
    mapView.delegate = self
    mapView.showsCompass = false
    
    getMapAnnotations()
  }
  
  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }
  
  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    self.videoCapture = nil
    self.objectDetection = nil
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .darkContent
  }
  
  func getMapAnnotations() {
    var id = 0
    
    let pListFileURL = Bundle.main.url(forResource: "dataset", withExtension: "plist", subdirectory: "")
    
    if let pListPath = pListFileURL?.path,
       let pListData = FileManager.default.contents(atPath: pListPath) {
      
      do {
        let pListArray = try PropertyListSerialization.propertyList(from: pListData, options:PropertyListSerialization.ReadOptions(), format:nil) as! [Dictionary<String, AnyObject>]
        
        for item in pListArray {
          let lat = item["Latitude"] as! Double
          let long = item["Longitude"] as! Double
          
          let annotation = Station(latitude: lat , longitude: long)
          annotation.id = id
          annotation.title = item["Name"] as? String
          annotation.subtitle = item["Type"] as? String
          annotation.type = "dataset"
          
          annotations.append(annotation)
          
          id += 1
        }
        
        if let url = URL(string: "https://api.3finery.com/LisNav/getEvents.php") {
           URLSession.shared.dataTask(with: url) { data, response, error in
              if let data = data {
                  do {
                    let res = try JSONDecoder().decode([Response].self, from: data)
                    
                    for item in res {
                      let lat = item.latitude
                      let long = item.longitude
                      
                      let annotation = Station(latitude: lat , longitude: long)
                      annotation.id = id
                      annotation.title = item.name
                      annotation.subtitle = NSLocalizedString("audioNote", comment: "audioNote") + NSLocalizedString(item.type, comment: item.type) + NSLocalizedString("audioNoteDescription", comment: "audioNote");
                      annotation.type = "user"
                      
                      self.annotations.append(annotation)
                    }
                    
                    self.hasURLData = true
                  } catch let error {
                     print(error)
                  }
               }
           }.resume()
        }
      } catch {
        print("Error reading regions plist file: \(error)")
      }
    }
  }
  
  func mapView(_ mapView: MKMapView, didUpdate userLocation: MKUserLocation) {
    if(self.hasURLData && !self.hasDescribedPosition) {
      mapView.addAnnotations(annotations)
      self.hasDescribedPosition = true
      
      getAddressFromLatLon(userLocation: userLocation)
    }
  }
  
  func calculateUserAngle(user: CLLocation, annotation: CLLocation, heading: CLLocationDirection?) -> String {
    let deltaL = annotation.coordinate.longitude.toRadians - user.coordinate.longitude.toRadians
    let thetaB = annotation.coordinate.latitude.toRadians
    let thetaA = user.coordinate.latitude.toRadians
    
    let x = cos(thetaB) * sin(deltaL)
    let y = cos(thetaA) * sin(thetaB) - sin(thetaA) * cos(thetaB) * cos(deltaL)
    
    let bearing = atan2(x,y)
    let bearingInDegrees = bearing.toDegrees

    let bearingFromMe = bearingInDegrees - (heading ?? 0)
    
    var text : String = ""
    let degrees = 360 + bearingFromMe
    
    if(degrees > 0 && degrees <= 90) {
      text = NSLocalizedString("audioFrontRight", comment: "audioFrontRight")
    } else if(degrees > 90 && degrees <= 180) {
      text = NSLocalizedString("audioBackRight", comment: "audioBackRight")
    } else if(degrees > 180 && degrees <= 270) {
      text = NSLocalizedString("audioBackLeft", comment: "audioBackLeft")
    } else if(degrees > 270 && degrees <= 360) {
      text = NSLocalizedString("audioFrontLeft", comment: "audioFrontLeft")
    }
    
    return text
  }
  
  func calculateOutputChannel(user: CLLocation, annotation: CLLocation, heading: CLLocationDirection?) -> AudioChannelLabel? {
    let deltaL = annotation.coordinate.longitude.toRadians - user.coordinate.longitude.toRadians
    let thetaB = annotation.coordinate.latitude.toRadians
    let thetaA = user.coordinate.latitude.toRadians
    
    let x = cos(thetaB) * sin(deltaL)
    let y = cos(thetaA) * sin(thetaB) - sin(thetaA) * cos(thetaB) * cos(deltaL)
    
    let bearing = atan2(x,y)
    let bearingInDegrees = bearing.toDegrees

    let bearingFromMe = bearingInDegrees - (heading ?? 0)
    let degrees = 360 + bearingFromMe
      
    if(degrees > 0 && degrees <= 180) {
      return kAudioChannelLabel_Right
    } else {
      return kAudioChannelLabel_Left
    }
  }
  
  func getAddressFromLatLon(userLocation: MKUserLocation) {
    let geocoder : CLGeocoder = CLGeocoder()
    let location = CLLocation(latitude: (userLocation.location?.coordinate.latitude)!, longitude: (userLocation.location?.coordinate.longitude)!)
    
    geocoder.reverseGeocodeLocation(location, completionHandler: {(placemarks, error) in
      if (error != nil) {
        print("reverse geodcode fail: \(error!.localizedDescription)")
      }
      
      let pm = placemarks as [CLPlacemark]?
      
      if pm != nil {
        if pm!.count > 0 {
          let pm = placemarks![0]
          
          self.getAnnotationsFromLatLong(userLocation: userLocation, streetAddress: pm.name)
        }
      }
    })
  }
  
  func getAnnotationsFromLatLong(userLocation: MKUserLocation, streetAddress: String?) {
    let location = CLLocation(latitude: (userLocation.location?.coordinate.latitude)!, longitude: (userLocation.location?.coordinate.longitude)!)
    
    if(userLocation.heading?.magneticHeading != nil) {
      var nearbyAnnotations:Array = [Station]()
      
      for item in annotations {
        let annotation = CLLocation(latitude: item.latitude, longitude: item.longitude)
        let distance = location.distance(from: annotation)
        
        if(distance < distanceThreshold) {
          nearbyAnnotations.append(item)
        }
      }
      
      var voiceOverText = ""
      var voiceOverIds : [Int] = []
      
      if(streetAddress != nil) {
        voiceOverText = NSLocalizedString("audioPosition", comment: "audioPosition") + streetAddress! + ". "
      }
      
      for item in nearbyAnnotations {
        let annotation = CLLocation(latitude: item.latitude, longitude: item.longitude)
        
        if(item.type == "dataset") {
          voiceOverText += calculateUserAngle(user: location, annotation: annotation, heading: userLocation.heading?.trueHeading)
        }
        
        voiceOverText += item.subtitle! + ", " + item.title! + ". "
        voiceOverIds.append(item.id!)
      }
      
      let utterance = AVSpeechUtterance(string: voiceOverText)
      utterance.voice = AVSpeechSynthesisVoice(language: NSLocalizedString("audioLanguage", comment: "audioLanguage"))

      synthesizer.speak(utterance)
      
      idsLastTTS = voiceOverIds
      isProcessing = false
      
      self.objectDetection.canRecognizeObjects = true
    }
  }
  
  func initalizeSpeechForChannel(outputChannel: AudioChannelLabel) -> [AVAudioSessionChannelDescription]? {
    let session = AVAudioSession.sharedInstance()
    try! session.setCategory(.playAndRecord, mode: .default, options: .allowBluetooth)
    
    var hasHandsFree : Bool = false
    
    if(session.availableInputs != nil) {
      for input in session.availableInputs! {
          if input.portType == .bluetoothHFP {
            hasHandsFree = true
            
            break
          }
      }
    }
    
    let route = session.currentRoute
    let outputPorts = route.outputs
    
    var channels:[AVAudioSessionChannelDescription] = []
    
    if(hasHandsFree) {
      for outputPort in outputPorts {
        for channel in outputPort.channels! {
          
          if channel.channelLabel == outputChannel {
            channels.append(channel)
          }
        }
      }
    } else {
      for outputPort in outputPorts {
        for channel in outputPort.channels! {
          if channel.channelLabel == kAudioChannelLabel_Center {
            channels.append(channel)
          }
        }
      }
    }
    
    return channels
  }
}
