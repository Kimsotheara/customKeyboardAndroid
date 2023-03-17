//
//  ViewController.swift
//  Consumer
//
//  Created by KLSoft on 9/3/22.
//  Copyright © 2022 Kim & Lim Soft Co. ltd. All rights reserved.
//

import UIKit
import SecureKeyboard

class ViewController: UIViewController, KeyboardDelegate {

    var textKeyboard: TextKeyboard?
    var numberKeybaord: NumberKeyboard?
    @IBOutlet weak var txtPwd: UITextField!
    @IBOutlet weak var txtName: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        textKeyboard = TextKeyboard(isShowKeyLogo: true, keyGap: 2)
        textKeyboard?.delegate=self
        txtPwd.inputView = textKeyboard
    }
   
    
    @IBAction func login(_ sender: UIButton) {
        let userName: String = txtName.text ?? ""
        let password: String = textKeyboard?.getText(txtPwd) ?? ""
        print("pwd: "+password)
        if(userName == "admin" && HashUtils.pwdDecoder(password) == "pwd"){
            let storyboard = UIStoryboard(name: "Main", bundle: nil)
            let vc = storyboard.instantiateViewController(withIdentifier: "welcome")
            //self.show(vc, sender: true)
            if #available(iOS 13.0, *) {
                (UIApplication.shared.connectedScenes.first?.delegate as? SceneDelegate)?.changeRootViewController(vc)
            }else{
                self.present(vc, animated: true)
            }
        }else{
            let alert = UIAlertController(title: "Alert", message: "Incorrect username or password", preferredStyle: UIAlertController.Style.alert)
            alert.addAction(UIAlertAction(title: "OK", style: UIAlertAction.Style.default, handler: nil))
            self.show(alert, sender: true)
        }
    }
    

    @IBAction func pinLogin(_ sender: UIButton) {
        
        
    }
    func keyWasTapped(action: KeyAction, character: String) {
        textKeyboard?.textFieldOperation(action: action, character: character, textFields: txtPwd)
    
    }
    
}

