//
//  WelcomScreen.swift
//  Consumer
//
//  Created by KLSoft on 29/3/22.
//  Copyright © 2022 Kim & Lim Soft Co. ltd. All rights reserved.
//

import UIKit
import SecureKeyboard

class WelcomScreen: UIViewController{
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    @IBAction func logout(_ sender: UIButton) {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let vc = storyboard.instantiateViewController(withIdentifier: "main")
        if #available(iOS 13.0, *) {
            (UIApplication.shared.connectedScenes.first?.delegate as? SceneDelegate)?.changeRootViewController(vc)
        }else{
            self.present(vc, animated: true)
        }
    }
    
}
