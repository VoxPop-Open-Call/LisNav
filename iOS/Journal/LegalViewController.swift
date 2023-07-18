import UIKit

class LegalViewController: UIViewController {
  
  @IBOutlet weak var acceptButton: UIButton!
  @IBOutlet weak var textLegal: UITextView!
  @IBOutlet weak var textLegalNote: UILabel!
  
  override func viewDidLoad() {
    super.viewDidLoad()

    self.textLegal.text = NSLocalizedString("legal", comment: "legal")
    self.textLegalNote.text = NSLocalizedString("legalNote", comment: "legalNote")
    self.acceptButton.setTitle(NSLocalizedString("legalAccept", comment: "legalAccept"), for: .normal)
  }
  
  override func viewDidAppear(_ animated: Bool) {
    let defaults = UserDefaults.standard
    let acceptedTerms = defaults.bool(forKey: "legalTerms")

    if(acceptedTerms) {
      performSegue(withIdentifier: "legalToHome", sender: nil)
    } else {
      setGradientBackground()
      
      self.textLegal.alpha = 1
      self.textLegalNote.alpha = 1
      self.acceptButton.alpha = 1
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
  @IBAction func acceptLegalTerms(_ sender: Any) {
    let defaults = UserDefaults.standard
    defaults.set(true, forKey: "legalTerms")
    
    performSegue(withIdentifier: "legalToHome", sender: nil)
  }
}
