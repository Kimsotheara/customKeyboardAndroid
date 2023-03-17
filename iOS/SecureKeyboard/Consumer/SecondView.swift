//
//  SecondView.swift
//  Consumer
//
//  Created by KLSoft on 28/3/22.
//  Copyright © 2022 Kim & Lim Soft Co. ltd. All rights reserved.
//

import SwiftUI

@available(iOS 13.0, *)
struct SecondView: UIView {
    let name: String
   
    var body: some UIView {
        Text("Hello, \(name)!")
    }
}
