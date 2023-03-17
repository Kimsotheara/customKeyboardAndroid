//
//  KeyboardDelegate.swift
//  SecureKeyboard
//
//  Created by KLSoft on 9/3/22.
//  Copyright © 2022 Kim & Lim Soft Co. ltd. All rights reserved.
//

public protocol KeyboardDelegate: AnyObject {
    func keyWasTapped(action: KeyAction, character: String)
}
