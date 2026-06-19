package com.example.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    // Use a fixed 128-bit key and IV for demonstration in the university project
    private val KEY = SecretKeySpec("SecureMedKey1234".toByteArray(Charsets.UTF_8), "AES")
    private val IV = IvParameterSpec("SecureMedIV98765".toByteArray(Charsets.UTF_8))

    /**
     * Encrypts plain text string to encrypted cipher text encoded in Base64
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, KEY, IV)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plainText
        }
    }

    /**
     * Decrypts Base64 encoded cipher text string to plain text
     */
    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        return try {
            val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, KEY, IV)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedText
        }
    }
}
