//
//  CheckSum.swift
//  SecureKeyboard
//
//  Created by KLSoft on 5/4/22.
//  Copyright © 2022 Kim & Lim Soft Co. ltd. All rights reserved.
//


public class HashUtils{
    static func pwdDecoder(_ pwd: String) -> String{
        let chksumLen: Int = 8
        let tmsLen: Int = 8
        let timeStp:String = String(pwd.prefix(tmsLen))
        let timeArr: [UInt8] = Array(timeStp.utf8)
        //convert 8-digit time stamp to crc8
        let timeCRC8: UInt8 = HashUtils.crc8(Array(timeArr))
        //let checksumLength:String = String(pwd.suffix(from: pwd.firstIndex(of: "L")!)

        //guard let LIndex = pwd.firstIndex(of: "L") else { return "" }
        //let nextIndex = pwd.index(LIndex, offsetBy: 1)
        //let hashChecksumLength = String(pwd.suffix(from: nextIndex))
        //checkSumLength = HashUtils.toInt(unsigned: UInt(UInt8(hashChecksumLength)! ^ timeCRC8))
        //LIndex = end index of checksum
        let endCheckSumIndex = pwd.endIndex
        let startCheckSumIndex = pwd.index(endCheckSumIndex, offsetBy: -chksumLen)
        //let end = pwd.index(lIndex, offsetBy: -1) //index before L
        let checkSumRange = startCheckSumIndex..<endCheckSumIndex
        let checksum_data = pwd[checkSumRange]
        //8 is last index of timestamp
        let startEnvDataIndex = pwd.index(pwd.startIndex, offsetBy: 8)
        let dataRange = startEnvDataIndex..<startCheckSumIndex
        
        guard let env_data = HashUtils.stringToBytes(String(pwd[dataRange])) else {
            return "encode data is nil"
        }
    
        var dec_v:[UInt8] = []
        var i = 0
        //XOR input value's character with CRC8 of time stamp
        for c in env_data {
            dec_v.insert(c ^ timeCRC8, at: i)
            i += 1
        }
        //Compare checksum, return if wrong checksum
        if(String(checksum_data) != self.uint32ToStrHex(self.crc32(bytes: dec_v))){
            return "wrong checksum"
        }
        //convert bytes to original string
        if let string = String(bytes: dec_v, encoding: String.Encoding.utf8) {
            return string
        } else {
            return "error converting bytes to string"
        }
    }
    static func uint32ToStrHex(_ uint32: UInt32) -> String{
        return String(format:"%02X", uint32)
    }
    static func stringToBytes(_ string: String) -> [UInt8]? {
        let length = string.count
        if length & 1 != 0 {
            return nil
        }
        var bytes = [UInt8]()
        bytes.reserveCapacity(length/2)
        var index = string.startIndex
        for _ in 0..<length/2 {
            let nextIndex = string.index(index, offsetBy: 2)
            if let b = UInt8(string[index..<nextIndex], radix: 16) {
                bytes.append(b)
            } else {
                return nil
            }
            index = nextIndex
        }
        return bytes
    }
    
    static func toUint8(signed: Int) -> UInt8 {

        let unsigned = signed >= 0 ?
            UInt8(signed) :
            UInt8(signed  - Int.min) + UInt8(Int.max) + 1

        return unsigned
    }
    
    static func toInt(unsigned: UInt) -> Int {

        let signed = (unsigned <= UInt(Int.max)) ?
            Int(unsigned) :
            Int(unsigned - UInt(Int.max) - 1) + Int.min

        return signed
    }
    
    static var table: [UInt32] = {
        (0...255).map { i -> UInt32 in
            (0..<8).reduce(UInt32(i), { c, _ in
                (c % 2 == 0) ? (c >> 1) : (0xEDB88320 ^ (c >> 1))
            })
        }
    }()

    static func crc32(bytes: [UInt8]) -> UInt32 {
        return ~(bytes.reduce(~UInt32(0), { crc, byte in
            (crc >> 8) ^ table[(Int(crc) ^ Int(byte)) & 0xFF]
        }))
    }
    
    static func crc8(_ buf : [UInt8]) -> UInt8 {
        let tableCRC8 : [UInt8] = [
            0x00, 0x07, 0x0E, 0x09, 0x1C, 0x1B, 0x12, 0x15,0x38, 0x3F, 0x36, 0x31, 0x24, 0x23, 0x2A, 0x2D,
            0x70, 0x77, 0x7E, 0x79, 0x6C, 0x6B, 0x62, 0x65,0x48, 0x4F, 0x46, 0x41, 0x54, 0x53, 0x5A, 0x5D,
            0xE0, 0xE7, 0xEE, 0xE9, 0xFC, 0xFB, 0xF2, 0xF5, 0xD8, 0xDF, 0xD6, 0xD1, 0xC4, 0xC3, 0xCA, 0xCD,
            0x90, 0x97, 0x9E, 0x99, 0x8C, 0x8B, 0x82, 0x85,0xA8, 0xAF, 0xA6, 0xA1, 0xB4, 0xB3, 0xBA, 0xBD,
            0xC7, 0xC0, 0xC9, 0xCE, 0xDB, 0xDC, 0xD5, 0xD2, 0xFF, 0xF8, 0xF1, 0xF6, 0xE3, 0xE4, 0xED, 0xEA,
            0xB7, 0xB0, 0xB9, 0xBE, 0xAB, 0xAC, 0xA5, 0xA2,0x8F, 0x88, 0x81, 0x86, 0x93, 0x94, 0x9D, 0x9A,
            0x27, 0x20, 0x29, 0x2E, 0x3B, 0x3C, 0x35, 0x32,0x1F, 0x18, 0x11, 0x16, 0x03, 0x04, 0x0D, 0x0A,
            0x57, 0x50, 0x59, 0x5E, 0x4B, 0x4C, 0x45, 0x42,0x6F, 0x68, 0x61, 0x66, 0x73, 0x74, 0x7D, 0x7A,
            0x89, 0x8E, 0x87, 0x80, 0x95, 0x92, 0x9B, 0x9C,0xB1, 0xB6, 0xBF, 0xB8, 0xAD, 0xAA, 0xA3, 0xA4,
            0xF9, 0xFE, 0xF7, 0xF0, 0xE5, 0xE2, 0xEB, 0xEC, 0xC1, 0xC6, 0xCF, 0xC8, 0xDD, 0xDA, 0xD3, 0xD4,
            0x69, 0x6E, 0x67, 0x60, 0x75, 0x72, 0x7B, 0x7C,0x51, 0x56, 0x5F, 0x58, 0x4D, 0x4A, 0x43, 0x44,
            0x19, 0x1E, 0x17, 0x10, 0x05, 0x02, 0x0B, 0x0C,0x21, 0x26, 0x2F, 0x28, 0x3D, 0x3A, 0x33, 0x34,
            0x4E, 0x49, 0x40, 0x47, 0x52, 0x55, 0x5C, 0x5B,0x76, 0x71, 0x78, 0x7F, 0x6A, 0x6D, 0x64, 0x63,
            0x3E, 0x39, 0x30, 0x37, 0x22, 0x25, 0x2C, 0x2B,0x06, 0x01, 0x08, 0x0F, 0x1A, 0x1D, 0x14, 0x13,
            0xAE, 0xA9, 0xA0, 0xA7, 0xB2, 0xB5, 0xBC, 0xBB,0x96, 0x91, 0x98, 0x9F, 0x8A, 0x8D, 0x84, 0x83,
            0xDE, 0xD9, 0xD0, 0xD7, 0xC2, 0xC5, 0xCC, 0xCB, 0xE6, 0xE1, 0xE8, 0xEF, 0xFA, 0xFD, 0xF4, 0xF3 ];
        
        
        var crc = UInt8.min
        
        for byte in buf {
            let x = crc ^ byte
            crc = tableCRC8[Int(x)]
        }
        
        return crc
    }
}
