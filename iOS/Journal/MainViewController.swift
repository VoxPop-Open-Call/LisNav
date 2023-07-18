import UIKit
import MapKit
import AVFoundation
import SwiftUI
import MediaPlayer
import StoreKit

struct Response: Codable {
  var id: Int
  var name: String
  var type: String
  var latitude: Double
  var longitude: Double
}

class Station: NSObject, MKAnnotation {
  var id: Int?
  var title: String?
  var subtitle: String?
  var type: String?
  var latitude: Double
  var longitude: Double
  
  var coordinate: CLLocationCoordinate2D {
    return CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
  }

  init(latitude: Double, longitude: Double) {
    self.latitude = latitude
    self.longitude = longitude
  }
}

class MainViewController: UIViewController, MKMapViewDelegate {
  @IBOutlet weak var mapView: MKMapView!
  @IBOutlet weak var locationbutton: UIButton!
  
  var annotations : [Station] = []
  var idsLastTTS : [Int] = []
  var isProcessing : Bool = false
  var actualUserLocation : MKUserLocation?
  
  let distanceThreshold : Double = 100
  let synthesizer = AVSpeechSynthesizer()
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    annotations = getMapAnnotations()
    mapView.addAnnotations(annotations)
    mapView.userTrackingMode = .followWithHeading
    mapView.delegate = self
    
    setGradientBackground()
    
    MPVolumeView.setVolume(1.0)
    
    if (try? AVAudioSession.sharedInstance().setCategory(.playback)) != nil {
      if (try? AVAudioSession.sharedInstance().setActive(true)) != nil {
        print("Ensuring silent mode is disabled")
      }
    }
    
    let defaults = UserDefaults.standard
    
    var count = defaults.integer(forKey: "openedTimes")
    let lastVersionPromptedForReview = defaults.string(forKey: "openedVersion")
    
    count += 1
    defaults.set(count, forKey: "openedTimes")
    
    let infoDictionaryKey = kCFBundleVersionKey as String
    guard let currentVersion = Bundle.main.object(forInfoDictionaryKey: infoDictionaryKey) as? String else { fatalError("Expected to find a bundle version in the info dictionary.") }
    
    if count >= 2 && currentVersion != lastVersionPromptedForReview {
      Task {
        @MainActor [weak self] in try? await Task.sleep(nanoseconds: UInt64(2e9))

        if let windowScene = self?.view.window?.windowScene {
          SKStoreReviewController.requestReview(in: windowScene)
             
          defaults.set(currentVersion, forKey: "openedVersion")
        }
      }
    }
  }

  func getMapAnnotations() -> [Station] {
    var annotations:Array = [Station]()
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
          
          annotations.append(annotation)
          
          id += 1
        }
      } catch {
        print("Error reading regions plist file: \(error)")
      }
    }
    
    return annotations
  }
  
  @IBAction func stopSpeaking(_ sender: Any) {
    if(self.synthesizer.isSpeaking) {
      self.synthesizer.stopSpeaking(at: .immediate)
    }
  }
  
  func setGradientBackground() {
    let colorTop =  UIColor(red: 17.0/255.0, green: 182.0/255.0, blue: 213.0/255.0, alpha: 1.0).cgColor
    let colorBottom = UIColor(red: 121.0/255.0, green: 99.0/255.0, blue: 171.0/255.0, alpha: 1.0).cgColor
                
    let gradientLayer = CAGradientLayer()
    gradientLayer.colors = [colorTop, colorBottom]
    gradientLayer.locations = [0.0, 1.0]
    gradientLayer.frame = self.view.bounds
            
    self.view.layer.insertSublayer(gradientLayer, at:0)
  }
  
  func mapView(_ mapView: MKMapView, didUpdate userLocation: MKUserLocation) {
    actualUserLocation = userLocation
  }
  
  @IBAction func locateMe(_ sender: Any) {
    locationbutton.titleLabel?.font = UIFont.systemFont(ofSize: 25, weight: .semibold)
    
    if(!isProcessing) {
      if(actualUserLocation != nil) {
        isProcessing = true
        
        getAddressFromLatLon(userLocation: actualUserLocation!)
      } else {
        let utterance = AVSpeechUtterance(string: NSLocalizedString("audioNoLocation", comment: "audioNoLocation"))
        utterance.voice = AVSpeechSynthesisVoice(language: NSLocalizedString("audioLanguage", comment: "audioLanguage"))

        synthesizer.speak(utterance)
      }
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
        
        voiceOverText += calculateUserAngle(user: location, annotation: annotation, heading: userLocation.heading?.trueHeading)
        voiceOverText += item.subtitle! + ", " + item.title! + ". "
        
        voiceOverIds.append(item.id!)
      }
      
      if(!synthesizer.isSpeaking) {
        let utterance = AVSpeechUtterance(string: voiceOverText)
        utterance.voice = AVSpeechSynthesisVoice(language: NSLocalizedString("audioLanguage", comment: "audioLanguage"))

        synthesizer.speak(utterance)
        idsLastTTS = voiceOverIds
      }
      
      isProcessing = false
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

extension Array where Element: Comparable {
    func containsSameElements(as other: [Element]) -> Bool {
        return self.count == other.count && self.sorted() == other.sorted()
    }
}

extension Double {
    var toRadians : Double {
        var m = Measurement(value: self, unit: UnitAngle.degrees)
        m.convert(to: .radians)
        return m.value
    }
    var toDegrees : Double {
        var m = Measurement(value: self, unit: UnitAngle.radians)
        m.convert(to: .degrees)
        return m.value
    }
}

extension MPVolumeView {
    static func setVolume(_ volume: Float) {
        let volumeView = MPVolumeView()
        let slider = volumeView.subviews.first(where: { $0 is UISlider }) as? UISlider

        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.01) {
            slider?.value = volume
        }
    }
}
