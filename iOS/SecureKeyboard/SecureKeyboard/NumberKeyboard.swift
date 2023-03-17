//
//  NumberKeyboard.swift
//  SecureKeyboard
//
//  Created by KLSoft on 9/3/22.
//  Copyright © 2022 Kim & Lim Soft Co. ltd. All rights reserved.
//

import UIKit

public class NumberKeyboard: UIView {
    public weak var delegate: KeyboardDelegate?
    var keyboardView: UIView!
    var keys: [UIButton] = []
    var textField: UITextField?
    var backspaceTimer: Timer?
    var input_values: [UITextField: String] = [UITextField: String]()
    //0: show empty key with logo icon, 1: show empty key without logo icon
    // 2: do not show empty key
    var keyboardTemplateOption: Int = 0
    public static var LAYOUT_EMPTY_KEY_NO_ICON: Int = 0
    public static var LAYOUT_EMPTY_KEY_WITH_ICON: Int = 1
    public static var LAYOUT_NO_EMPTY_KEY: Int = 2
    var isShowKeyLogo: Bool = false
    var keyGap: Int = 0
    @IBOutlet weak var stackView1: UIStackView!
    @IBOutlet weak var stackView2: UIStackView!
    @IBOutlet weak var stackView3: UIStackView!
    @IBOutlet weak var stackView4: UIStackView!
    @IBOutlet weak var stackView5: UIStackView!
    
    var btnNumbers = (0..<10).map {_ in UIButton()}
    var ranBgIndex1: Int = 0
    var ranBgIndex2: Int = 0
    public init(keyboardTemplateOption: Int, keyGap: Int) {
        super.init(frame: CGRect(x: 0, y: 0, width: 350, height: 250))
        initializeSubviews()
        self.keyGap = keyGap;
        self.keyboardTemplateOption = keyboardTemplateOption
        if keyboardTemplateOption == NumberKeyboard.LAYOUT_EMPTY_KEY_WITH_ICON {
            self.isShowKeyLogo = true
        }else{
            self.isShowKeyLogo = false
        }
    }
    
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        initializeSubviews()
    }
    
    // orientation handling
    public override func layoutSubviews() {
        super.layoutSubviews()
        loadKeys()
    }
    
    func initializeSubviews() {
        let bundlePath = Bundle.main.path(forResource: "ResourceBundle", ofType: "bundle")!
        let bundle = Bundle(path: bundlePath)!
        let keyboardNib = UINib(nibName: "Keyboard", bundle: bundle)
//        let keyboardNib:UINib
//        if(keyboardTemplateOption == NumberKeyboard.LAYOUT_NO_EMPTY_KEY){
//              keyboardNib = UINib(nibName: "Keyboard_1", bundle: bundle)
//        }else{
//              keyboardNib = UINib(nibName: "Keyboard", bundle: bundle)
//        }
        keyboardView = keyboardNib.instantiate(withOwner: self, options: nil)[0] as? UIView
        self.addSubview(keyboardView)
        loadKeys()
    }
    /**Generate keys' button and add to stackview
     */
    func loadKeys(){
        //Unlinks the UIButton from its superview
        keys.forEach{$0.removeFromSuperview()}
        var numberKeyPad: [[String]]
        if(keyboardTemplateOption == NumberKeyboard.LAYOUT_NO_EMPTY_KEY){
            numberKeyPad = Constants.numberPadKeys1
        }else{
            numberKeyPad = Constants.numberPadKeys0
        }
        let buttonWidth = ((UIScreen.main.bounds.width) / CGFloat(numberKeyPad[1].count))
        //Create two dimension array for storing random numberPadkey
        var keyboard: [[String]]
        //Random keys
        keyboard = shuffleKeys(numberKeyPad);
            
        if(keyboardTemplateOption == NumberKeyboard.LAYOUT_NO_EMPTY_KEY){
            keyboard[3].insert(Constants.controlKeys[0][2], at: 0) // add delete key
            keyboard[3].insert(Constants.controlKeys[0][4], at: 2) // add enter key
        }else{
            keyboard.insert(Constants.controlKeys[1],at:4)
        }
        let numRows = keyboard.count
        for row in 0...numRows - 1{
            for col in 0...keyboard[row].count - 1{
                let key = keyboard[row][col]
                let button = UIButton(type: .custom)
                    switch(key){
                    case "0", "1", "2","3","4","5","6","7","8","9":
                        setKeyProperties(button: btnNumbers[Int(key)!], key: key, row: row, buttonWidth: Int(buttonWidth))
                        break
                    default:
                        setKeyProperties(button: button, key: key, row: row, buttonWidth: Int(buttonWidth))
                        break;
                    }
            }
        }
    }
    func setKeyProperties(button: UIButton, key: String, row: Int, buttonWidth: Int){
        button.backgroundColor = Constants.keyNormalColour
        button.setTitleColor(.black, for: .normal)
        button.layer.setValue(key, forKey: "original")
        button.layer.setValue(key, forKey: "keyToDisplay")
        button.layer.setValue(false, forKey: "isSpecial")
        button.layer.borderColor = keyboardView.backgroundColor?.cgColor
        button.layer.borderWidth = 4
        button.addTarget(self, action: #selector(keyPressedTouchUp), for: .touchUpInside)
        button.addTarget(self, action: #selector(keyTouchDown), for: .touchDown)
//                button.addTarget(self, action: #selector(keyUntouched), for: .touchDragExit)
        button.layer.cornerRadius = 10;
        
        if key == "" {
            button.isEnabled = false
            if(isShowKeyLogo){
                setKeyImage(button: button, imgName: "empty_key_icon", state: .disabled)
            }else{
                button.backgroundColor = Constants.emptyKeyColor
            }

        }
        if key == "♻︎" {
            button.setTitle("재배열", for: .normal)
        }
        else if key == "⏎" {
            button.setTitle("입력완료", for: .normal)
        }else{
            button.setTitle(key, for: .normal )
        }
        if key == "⌫" {
            let longPressRecognizer = UILongPressGestureRecognizer(target: self, action: #selector(keyLongPressed(_:)))
            button.addGestureRecognizer(longPressRecognizer)
        }
        //Add button to UIButton Array so that we can use keys array to remove the UIbutton from superView
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
        button.widthAnchor.constraint(equalToConstant: CGFloat(buttonWidth) - CGFloat(keyGap)).isActive = true
        stackView1.heightAnchor.constraint(equalToConstant: CGFloat((50 - keyGap))).isActive = true
        stackView2.heightAnchor.constraint(equalToConstant: CGFloat((50 - keyGap))).isActive = true
        stackView3.heightAnchor.constraint(equalToConstant: CGFloat((50 - keyGap))).isActive = true
        stackView4.heightAnchor.constraint(equalToConstant: CGFloat((50 - keyGap))).isActive = true
        stackView5.heightAnchor.constraint(equalToConstant: CGFloat((50 - keyGap))).isActive = true
        
    
        //button.layer.setValue(key, forKey: "original")
        //button.setTitle(key, for: .normal)
    }
    func setKeyImage(button: UIButton, imgName: String, state:UIControl.State){
        let bundlePath = Bundle.main.path(forResource: "ResourceBundle", ofType: "bundle")!
        let bundle = Bundle(path: bundlePath)!
        let image = UIImage(named: imgName,
                            in: bundle,
                            compatibleWith: nil)
        button.setTitle("", for: state)
        button.setImage(image, for: state)
        //button.setBackgroundImage(image, for: state)

    }
    func shuffleKeys<T>(_ arr: [[T]]) -> [[T]] {
        var iter = arr.joined().shuffled().makeIterator()
        return arr.map { $0.compactMap { _ in iter.next() }}
    }
    
    @IBAction func keyPressedTouchUp(_ sender: UIButton) {
        guard let originalKey = sender.layer.value(forKey: "original") as? String, let keyToDisplay = sender.layer.value(forKey: "keyToDisplay") as? String else {return}
        //sender.backgroundColor = Constants.keyNormalColour
        btnNumbers[ranBgIndex1].backgroundColor = Constants.keyNormalColour
        btnNumbers[ranBgIndex2].backgroundColor = Constants.keyNormalColour
        switch originalKey {
        case "⌫":
            self.delegate?.keyWasTapped(action: KeyAction.delete, character: "⌫")
        case "⏎":
            self.delegate?.keyWasTapped(action: KeyAction.return, character: "⏎")
        case "♻︎":
            loadKeys()
//        case "✓":
//            self.delegate?.keyWasTapped(action: KeyAction.confirm, character: "t")
        case "":
            break
        default:
            self.delegate?.keyWasTapped(action: KeyAction.insert, character: keyToDisplay)
        }
        
    }
    
    
    @objc func keyLongPressed(_ gesture: UIGestureRecognizer){
        if gesture.state == .began {
            backspaceTimer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { (timer) in
                self.delegate?.keyWasTapped(action: KeyAction.delete, character: "⌫")
            }
            
        } else if gesture.state == .ended || gesture.state == .cancelled {
            backspaceTimer?.invalidate()
            backspaceTimer = nil
            (gesture.view as! UIButton).backgroundColor = Constants.specialKeyNormalColour
            textField?.isSecureTextEntry = true
        }
    }
    
    
    @objc func keyTouchDown(_ sender: UIButton){
        //sender.backgroundColor = Constants.keyPressedColour
        ranBgIndex1 = Int.random(in: 0..<10)
        ranBgIndex2 = Int.random(in: 0..<10)
        btnNumbers[ranBgIndex1].backgroundColor = Constants.keyPressedColour
        btnNumbers[ranBgIndex2].backgroundColor = Constants.keyPressedColour
//        let bundlePath = Bundle.main.path(forResource: "ResourceBundle", ofType: "bundle")!
//        let bundle = Bundle(path: bundlePath)!
//        let image = UIImage(named: "empty_key_icon",
//                            in: bundle,
//                            compatibleWith: nil)
//        keys[2].setBackgroundImage(image, for: .normal)
        guard let originalKey = sender.layer.value(forKey: "original") as? String else {return}
        if originalKey == "✓"{
            self.delegate?.keyWasTapped(action: KeyAction.confirm, character: "f")
        }
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
                    if(tmp == ""){//clear dictionary if textField is nil or text is ""
                        input_values[textField] = nil
                    }else{
                        textField.text = input_values[textField]
                        input_values[textField] = tmp
                    }
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
