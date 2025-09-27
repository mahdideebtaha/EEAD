package com.mahdideebtaha.eead.utils

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {

    // AES
    fun generateAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(128)
        return keyGen.generateKey()
    }

    fun encryptAES(data: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(data)
    }

    // RSA
    fun generateRSAKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }

    fun encryptRSA(data: ByteArray, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val maxSize = 200
        val trimmedData = data.copyOf(minOf(data.size, maxSize))
        return cipher.doFinal(trimmedData)
    }

    fun decryptRSA(data: ByteArray, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(data)
    }

    // Blowfish
    fun encryptBlowfish(data: ByteArray, key: String): ByteArray {
        val secretKey = SecretKeySpec(key.toByteArray(), "Blowfish")
        val cipher = Cipher.getInstance("Blowfish")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(data)
    }

    fun decryptBlowfish(data: ByteArray, key: String): ByteArray {
        val secretKey = SecretKeySpec(key.toByteArray(), "Blowfish")
        val cipher = Cipher.getInstance("Blowfish")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(data)
    }
}