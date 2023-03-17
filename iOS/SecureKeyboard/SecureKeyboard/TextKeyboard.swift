//
//  Keyboard.swift
//  SecureKeybard
//
//  Created by KLSoft on 9/3/22.
//  Copyright © 2022 Kim & Lim Soft Co. ltd. All rights reserved.
//

import UIKit
import zlib //crc library


extension Date {
    func toMillis() -> Int64! {
        return Int64(self.timeIntervalSince1970)
    }
}
extension Array where Element == UInt8 {
     func bytesToHex(spacing: String) -> String {
       var hexString: String = ""
       var count = self.count
       for byte in self
       {
           hexString.append(String(format:"%02X", byte))
           count = count - 1
           if count > 0
           {
               hexString.append(spacing)
           }
       }
       return hexString
    }
}

public class TextKeyboard: UIView {
    public weak var delegate: KeyboardDelegate?
    var keyboardView: UIView!
    var keys: [UIButton] = []
    var textField: UITextField?
    var backspaceTimer: Timer?
    var shiftTimer:Timer?
    var keyGap: Int = 0
    var input_values: [UITextField: String] = [UITextField: String]()
    var keyPressedValue: String?
    enum KeyboardState{
        case letters
        //case numbers
        case symbols
    }
    
    enum ShiftButtonState {
        case normal
        case shift
        case caps
    }
    
    var keyboardState: KeyboardState = .letters
    var shiftButtonState:ShiftButtonState = .normal
    /**
        0: No image for empty key
        1: Set image for emtpy key
     */
    var isShowKeyLogo: Bool = false
    
    @IBOutlet weak var stackView1: UIStackView!
    @IBOutlet weak var stackView2: UIStackView!
    @IBOutlet weak var stackView3: UIStackView!
    @IBOutlet weak var stackView4: UIStackView!
    @IBOutlet weak var stackView5: UIStackView!
    
    public init(isShowKeyLogo: Bool, keyGap: Int) {
        super.init(frame: CGRect(x: 0, y: 0, width: 350, height: 250))
        initializeSubviews()
        self.keyGap = keyGap
        self.isShowKeyLogo = isShowKeyLogo
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        initializeSubviews()
    }
    
    // orientation handling
    public override func layoutSubviews() {
        super.layoutSubviews()
        loadTextKeys()
    }
    
    func initializeSubviews() {
        let bundlePath = Bundle.main.path(forResource: "ResourceBundle", ofType: "bundle")!
        let bundle = Bundle(path: bundlePath)!
        let keyboardNib = UINib(nibName: "Keyboard", bundle: bundle)
        keyboardView = keyboardNib.instantiate(withOwner: self, options: nil)[0] as? UIView
        self.addSubview(keyboardView)
        loadTextKeys()
    }
    
    func loadTextKeys(){
        keys.forEach{$0.removeFromSuperview()}
        let buttonWidth = ((UIScreen.main.bounds.width - 40) / CGFloat(Constants.letterKeys[0].count))
        var keyboard: [[String]]
        
        switch keyboardState {
        case .letters:
            keyboard = Constants.letterKeys
            keyboard[3].insert(Constants.controlKeys[0][1], at: 0) // add shift key
            keyboard[3].insert(Constants.controlKeys[0][2], at: keyboard[3].count) // add delete key
            keyboard.insert(Constants.controlKeys[2], at: 4) // add control keys of 123
//        case .numbers:
//            keyboard = Constants.numberKeys
//            keyboard[2].insert(Constants.controlKeys[0][0], at: 0) // add #+= key
//            keyboard[2].insert(Constants.controlKeys[0][2], at: keyboard[2].count) // add delete key
//            keyboard.insert(Constants.controlKeys[2], at: 3) // add control keys of abc
        case .symbols:
            keyboard = Constants.symbolKeys
            keyboard.insert(Constants.controlKeys[3], at: 4)
//            keyboard[2].insert(Constants.controlKeys[1][0], at: 0) // add 123 key
            keyboard[3].insert(Constants.controlKeys[0][2], at: keyboard[3].count) // add delete key
//            keyboard.insert(Constants.controlKeys[2], at: 3) // add control keys of abc
        }
        
        //add blank button
        var blankKeyPos: Int
        blankKeyPos = Int.random(in: 0..<11)
        keyboard[0].insert("", at:blankKeyPos)
        if(keyboardState == .symbols){
            blankKeyPos = Int.random(in: 0..<9)
            keyboard[1].insert("", at:blankKeyPos)
            blankKeyPos = Int.random(in: 0..<10);
            keyboard[1].insert("", at:blankKeyPos)
        }
        blankKeyPos = Int.random(in: 0..<11);
        keyboard[1].insert("", at:blankKeyPos)
        if(keyboardState == .symbols ){
            blankKeyPos = Int.random(in: 0..<9)
            keyboard[2].insert("", at:blankKeyPos)
        }
        blankKeyPos = Int.random(in: 0..<10);
        keyboard[2].insert("", at:blankKeyPos)
        blankKeyPos = Int.random(in: 0..<11);
        keyboard[2].insert("", at:blankKeyPos)
        if(keyboardState == .symbols ){
            blankKeyPos = Int.random(in: 0..<6)
            keyboard[3].insert("", at:blankKeyPos)
            blankKeyPos = Int.random(in: 0..<7)
            keyboard[3].insert("", at:blankKeyPos)
            blankKeyPos = Int.random(in: 0..<8)
            keyboard[3].insert("", at:blankKeyPos)
        }else{
            blankKeyPos = Int.random(in: 1..<9)
            keyboard[3].insert("", at:blankKeyPos)
        }
        let numRows = keyboard.count
        for row in 0...numRows - 1{
            for col in 0...keyboard[row].count - 1{
                let key = keyboard[row][col]
                let capsKey = key.capitalized
                let keyToDisplay = shiftButtonState == .normal ? key : capsKey
                let button = UIButton(type: .custom)
                button.backgroundColor = Constants.keyNormalColour
                button.setTitle(keyToDisplay, for: .normal)
                button.setTitleColor(.black, for: .normal)
                button.layer.setValue(key, forKey: "original")
                button.layer.setValue(keyToDisplay, forKey: "keyToDisplay")
                button.layer.setValue(false, forKey: "isSpecial")
                button.layer.borderColor = keyboardView.backgroundColor?.cgColor
                button.layer.borderWidth = 4
                button.addTarget(self, action: #selector(keyPressedTouchUp), for: .touchUpInside)
                button.addTarget(self, action: #selector(keyTouchDown), for: .touchDown)
                button.addTarget(self, action: #selector(keyUntouched), for: .touchDragExit)
                button.addTarget(self, action: #selector(keyMultiPress(_:event:)), for: .touchDownRepeat)

                button.layer.cornerRadius = 10;
                if key == "" {
                    button.isEnabled = false
                    if(isShowKeyLogo){
                        setKeyImage(button: button, imgName: "empty_key_icon", state: .disabled)
                    }else{
                        button.backgroundColor = Constants.emptyKeyColor
                    }
                }else if key == "♻︎" {
                    button.setTitle("재배열", for: .normal)
                }
                else if key == "⏎" {
                    button.setTitle("입력완료", for: .normal)
                }else{
                    button.setTitle(key, for: .normal )
                }
                if key == "⇧" {
                    let longPressRecognizer = UILongPressGestureRecognizer(target: self, action: #selector(keyLongPressed(_:)))
                    button.addGestureRecognizer(longPressRecognizer)
//                    if(shiftButtonState == .caps){
//                        setKeyImage(button: button, imgName: "empty_key_icon", state: .normal)
//                    }
                }
                if key == "⌫" {
                    let longPressRecognizer = UILongPressGestureRecognizer(target: self, action: #selector(keyLongPressed(_:)))
                    button.addGestureRecognizer(longPressRecognizer)
                }
                
                keys.append(button)
                
                switch row {
                case 0: stackView1.addArrangedSubview(button)
                case 1: stackView2.addArrangedSubview(button)
                case 2: stackView3.addArrangedSubview(button)
                case 3: stackView4.addArrangedSubview(button)
                case 4: stackView5.addArrangedSubview(button)
                default:
                    break
                }
                stackView1.heightAnchor.constraint(equalToConstant: CGFloat((50 - keyGap))).isActive = true
                stackView2.heightAnchor.constraint(equalToConstant: CGFloat((50 - keyGap))).isActive = true
                stackView3.heightAnchor.constraint(equalToConstant: CGFloat((50 - keyGap))).isActive = true
                stackView4.heightAnchor.constraint(equalToConstant: CGFloat((50 - keyGap))).isActive = true
                stackView5.heightAnchor.constraint(equalToConstant: CGFloat((50 - keyGap))).isActive = true

                
                if key == "⇧" {
                    button.layer.setValue(true, forKey: "isSpecial")
                    button.backgroundColor = Constants.specialKeyNormalColour
                    if shiftButtonState != .normal{
                        button.backgroundColor = Constants.keyPressedColour
                    }
                }
                if key == "⌫" || key == "!@#" || key == "1Aa" || key == "♻︎" {
                    button.widthAnchor.constraint(equalToConstant: (buttonWidth * 2) - CGFloat(keyGap)).isActive = true
                    button.layer.setValue(true, forKey: "isSpecial")
                    button.backgroundColor = Constants.specialKeyNormalColour
                }
                else if key == "⏎"{
                    button.widthAnchor.constraint(equalToConstant: (buttonWidth * 3) - CGFloat(keyGap)).isActive = true
                    button.layer.setValue(true, forKey: "isSpecial")
                    button.backgroundColor = Constants.specialKeyNormalColour
                    
                }
                else if key == "space" {
                    button.widthAnchor.constraint(equalToConstant: (buttonWidth * 4) - CGFloat(keyGap)).isActive = true
                }
                else {
                    button.widthAnchor.constraint(equalToConstant: buttonWidth - CGFloat(keyGap)).isActive = true
                    //button.layer.setValue(key, forKey: "original")
                    //button.setTitle(key, for: .normal)
                }
                
            }
        }
    }
    
    func setKeyImage(button: UIButton, imgName: String, state:UIControl.State){
        let bundlePath = Bundle.main.path(forResource: "ResourceBundle", ofType: "bundle")!
        let bundle = Bundle(path: bundlePath)!
        let image = UIImage(named: imgName,
                            in: bundle,
                            compatibleWith: nil)
        button.setTitle("", for: .normal)
        button.setImage(image, for: state)

    }
   
//    func changeKeyboardToNumberKeys(){
//        keyboardState = .symbols
//        shiftButtonState = .normal
//        loadTextKeys()
//    }
    
    func changeKeyboardToLetterKeys() {
        keyboardState = .letters
        loadTextKeys()
    }
    
    func changeKeyboardToSymbolKeys() {
        keyboardState = .symbols
        loadTextKeys()
    }
    
    @IBAction func keyPressedTouchUp(_ sender: UIButton) {
        guard let originalKey = sender.layer.value(forKey: "original") as? String, let keyToDisplay = sender.layer.value(forKey: "keyToDisplay") as? String else {return}
        guard let isSpecial = sender.layer.value(forKey: "isSpecial") as? Bool else {return}
        sender.backgroundColor = isSpecial ? Constants.specialKeyNormalColour : Constants.keyNormalColour
        keyPressedValue = ""
        switch originalKey {
        case "⌫":
            if shiftButtonState == .shift {
                shiftButtonState = .normal
                loadTextKeys()
            }
            self.delegate?.keyWasTapped(action: KeyAction.delete, character: "⌫")
        case "space":
            self.delegate?.keyWasTapped(action: KeyAction.insert, character: " ")
        case "⏎":
            self.delegate?.keyWasTapped(action: KeyAction.return, character: "⏎")
        case "1Aa":
            changeKeyboardToLetterKeys()
        case "!@#":
            changeKeyboardToSymbolKeys()
        case "⇧":
            shiftButtonState = shiftButtonState == .normal ? .shift : .normal
            loadTextKeys()
        case "♻︎":
            loadTextKeys()
//        case "✓":
//            self.delegate?.keyWasTapped(action: KeyAction.confirm, character: "t")
        case "":
            break
        default:
            if shiftButtonState == .shift {
                shiftButtonState = .normal
                loadTextKeys()
            }
            self.delegate?.keyWasTapped(action: KeyAction.insert, character: keyToDisplay)
        }
    }
    
    @objc func keyMultiPress(_ sender: UIButton, event: UIEvent){
        //print("ShiftKeyPress: multi keypressed...")
        //guard let originalKey = sender.layer.value(forKey: "original") as? String else {return}
        //let touch: UITouch = event.allTouches!.first!
//        if (originalKey == "⇧") {
//            shiftButtonState = .caps
//            loadTextKeys()
//        }
    }
    
    @objc func keyLongPressed(_ gesture: UIGestureRecognizer){
        if gesture.state == .began {
            if(keyPressedValue == "⌫"){
                backspaceTimer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { (timer) in
                    self.delegate?.keyWasTapped(action: KeyAction.delete, character: "⌫")
                }
            }
            if(keyPressedValue == "⇧"){
                shiftTimer = Timer.scheduledTimer(withTimeInterval: 1, repeats: false){
                    (timer) in
                    self.shiftButtonState = .caps
                    self.loadTextKeys()
                }
            }
        } else if gesture.state == .ended || gesture.state == .cancelled {
            backspaceTimer?.invalidate()
            backspaceTimer = nil
            shiftTimer = nil
            (gesture.view as! UIButton).backgroundColor = Constants.specialKeyNormalColour
            
        }
    }
    
    @objc func keyUntouched(_ sender: UIButton){
               guard let isSpecial = sender.layer.value(forKey: "isSpecial") as? Bool else {return}
        sender.backgroundColor = isSpecial ? Constants.specialKeyNormalColour : Constants.keyNormalColour
    }
    
    @objc func keyTouchDown(_ sender: UIButton){
        sender.backgroundColor = Constants.keyPressedColour
        guard let originalKey = sender.layer.value(forKey: "original") as? String else {return}
        keyPressedValue = originalKey
//        if originalKey == "✓"{
//            self.delegate?.keyWasTapped(action: KeyAction.confirm, character: "f")
//        }
    }
        
    
    public func textFieldOperation(action: KeyAction, character: String, textFields: UITextField...) {
        for textField in textFields {
            //textField.isSecureTextEntry = true
            if textField.isFirstResponder {
                if action == KeyAction.delete {
                    deleteText(textField: textField)
                }else if action == KeyAction.return {
                    textField.resignFirstResponder()
                } else if action == KeyAction.confirm {
                    let tmp: String = textField.text ?? ""
                    textField.text = input_values[textField]
                    input_values[textField] = tmp
                }else {
                    //Chage real value to *
                    if(textField.text != "" ){
                        if var textRange = textField.selectedTextRange {
                            if textRange.isEmpty && textRange.start != textField.beginningOfDocument {
                                textRange = textField.textRange(from: textField.position(from: textRange.start, offset: -1)!, to: textRange.start)!
                            }
                            textField.replace(textRange, withText: "*")
                        }
                    }
                    //insert character to last position
                    textField.insertText(character)
                    //add real value to dictionary
                    if(input_values[textField] == nil){
                        input_values[textField] = character
                    }else{
                        let lastIndex: String.Index = input_values[textField]!.endIndex
                        input_values[textField]?.insert(contentsOf: character, at: lastIndex)
                    }
                }
            }
        }
    }

    func deleteText(textField: UITextField){
        if let selectedRange = textField.selectedTextRange {
            let cursorPosition = textField.offset(from: textField.beginningOfDocument, to: selectedRange.start)
            let textVal: String = textField.text ?? ""
            if(textVal != ""){
                let index = textVal.index(textVal.startIndex, offsetBy: cursorPosition-1)
                textField.text?.remove(at: index)
                input_values[textField]?.remove(at: index)
            }
        }
    }
    
    public func getText(_ textField: UITextField) -> String{
        defer{
            textField.text = ""
            input_values[textField] = nil
            input_values.removeValue(forKey: textField)
        }
        
        let curTimeStamp:Int64 = Date().toMillis()
        //Get last 8 digits of timestamp
        let strCurTimeStamp: String = String(String(curTimeStamp).suffix((8)))
        let timeArr: [UInt8] = Array(strCurTimeStamp.utf8)
        //convert 8-digit time stamp to crc8
        let timeCRC8: UInt8 = HashUtils.crc8(Array(timeArr))
        //get input value
        let input_v:String = input_values[textField] ?? ""
        var enc_v:[UInt8] = []
        //convert input value to UInt8 Array
        let byte_input_v = [UInt8] (input_v.utf8)
        var i = 0
        //XOR input value's character with CRC8 of time stamp
        for c in byte_input_v {
            enc_v.insert(c ^ timeCRC8, at: i)
            i += 1
        }
        //Create checksum value from input value's original bytes
        let checksumHex = HashUtils.uint32ToStrHex(HashUtils.crc32(bytes: byte_input_v))
        return strCurTimeStamp + enc_v.bytesToHex(spacing: "") + checksumHex
    }

}

