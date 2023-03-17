//
//  PinLoginViewController.swift
//  Consumer
//
//  Created by KLSoft on 29/3/22.
//  Copyright © 2022 Kim & Lim Soft Co. ltd. All rights reserved.
//

import UIKit
import SecureKeyboard

class PinLoginViewController: UIViewController, KeyboardDelegate{
    @IBOutlet weak var txtPin: UITextField!
    @IBOutlet weak var txtPin1: UITextField!
    var numberKeyboard: NumberKeyboard?
    override func viewDidLoad() {
        super.viewDidLoad()
        
        numberKeyboard = NumberKeyboard(keyboardTemplateOption: NumberKeyboard.LAYOUT_EMPTY_KEY_NO_ICON, keyGap: 0)
        numberKeyboard?.delegate = self
        txtPin.inputView = numberKeyboard
        txtPin.becomeFirstResponder()
        
        numberKeyboard = NumberKeyboard(keyboardTemplateOption: NumberKeyboard.LAYOUT_EMPTY_KEY_WITH_ICON, keyGap: 10)
        numberKeyboard?.delegate = self
        txtPin1.inputView = numberKeyboard
        txtPin1.becomeFirstResponder()
    }


    func keyWasTapped(action: KeyAction, character: String) {
        numberKeyboard?.textFieldOperation(action: action, character: character, textFields: txtPin, txtPin1)
        if(action == KeyAction.return || txtPin.text?.count == 4){
            login()
        }
    }

    func login(){
        let pwd:String = numberKeyboard!.getText(txtPin)
        if(HashUtils.pwdDecoder(pwd) == "1234"){
            let storyboard = UIStoryboard(name: "Main", bundle: nil)
            let vc = storyboard.instantiateViewController(withIdentifier: "welcome")
            // This is to get the SceneDelegate object from your view controller
            // then call the change root view controller function to change to main tab bar
            if #available(iOS 13.0, *) {
                (UIApplication.shared.connectedScenes.first?.delegate as? SceneDelegate)?.changeRootViewController(vc)
            }else{
                self.present(vc, animated: true)
            }
        }else{
            txtPin.becomeFirstResponder()
            let alert = UIAlertController(title: "Alert", message: "Incorrect pin", preferredStyle: UIAlertController.Style.alert)
            alert.addAction(UIAlertAction(title: "OK", style: UIAlertAction.Style.default, handler: nil))
            self.present(alert, animated: true)
            
        }
    }
}
