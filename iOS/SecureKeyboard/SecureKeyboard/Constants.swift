//
//  Constants.swift
//  SecureKeybard
//
//  Created by KLSoft on 9/3/22.
//  Copyright © 2022 Kim & Lim Soft Co. ltd. All rights reserved.
//

import UIKit

enum Constants{
    
    static let keyNormalColour: UIColor = .white
    static let keyPressedColour: UIColor = .green
    static let specialKeyNormalColour: UIColor = .lightGray
    static let emptyKeyColor: UIColor = .lightText

    static let letterKeys = [
        ["0","1","2","3","4","5","6","7","8","9"],
        ["q", "w", "e", "r", "t", "y", "u", "i", "o", "p"],
        ["a", "s", "d", "f", "g","h", "j", "k", "l"],
        ["z", "x", "c", "v", "b", "n", "m"]
    ]
    

    
    static let symbolKeys = [
        ["!", "@", "#", "$", "%", "^" ,"&", "*","(", ")"],
        ["~", "`", "_", "-", "=", "+","₩", "|"],
        [";", ",", "\'", "\"", "[", "]", "{", "}"],
        ["<", ">", ":", ".", "?", "/"]
    ]
    
    static let controlKeys = [
        ["!@#", "⇧", "⌫", "♻︎", "⏎"],
        ["♻︎","⌫","⏎"],
        ["♻︎","!@#","space","⏎"],
        ["♻︎","1Aa","space","⏎"]
    ]
    
    static let numberPadKeys0 = [
        ["1", "2", "3"],
        ["4", "5", ""],
        ["6", "7", "8"],
        ["9","0" ,""]
    ]
    static let numberPadKeys1 = [
        ["1", "2", "3"],
        ["4", "5", "6"],
        ["7","8", "9"],
        ["0" ]
    ]
}
