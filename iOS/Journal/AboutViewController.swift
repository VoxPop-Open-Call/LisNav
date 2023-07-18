import Foundation
import UIKit

class AboutViewController: UIViewController {
  
  @IBOutlet weak var closeButton: UIButton!
  @IBOutlet weak var titleLabel: UILabel!
  @IBOutlet weak var textView: UITextView!
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    self.overrideUserInterfaceStyle = .light
  
    self.closeButton.setTitle(NSLocalizedString("close", comment: "close"), for: .normal)
    self.titleLabel.text = NSLocalizedString("apptitle", comment: "apptitle")
    self.textView.text = NSLocalizedString("funding", comment: "funding")
    
    setGradientBackground()
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
}
