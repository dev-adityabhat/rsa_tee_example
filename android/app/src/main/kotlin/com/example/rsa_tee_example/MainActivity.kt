package com.example.rsa_tee_example

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class MainActivity : FlutterActivity() {
    private val CHANNEL = "flutter/rsa_tee"
    private val KEY_ALIAS = "flutter_tee_key"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "generateKeyPair" -> {
                    generateKeyPair()
                    result.success("Key pair generated successfully.")
                }
                "encrypt" -> {
                    val plaintext = call.argument<String>("text")
                    if (plaintext != null) {
                        val publicKey = getPublicKey()
                        val encryptedData = encryptStringWithPublicKey(plaintext, publicKey)
                        result.success(mapOf("publicKey" to publicKey, "encryptedData" to encryptedData))
                    } else {
                        result.error("INVALID_INPUT", "No text provided for encryption", null)
                    }
                }
                "decrypt" -> {
                    val encryptedText = call.argument<String>("encryptedData")
                    if (encryptedText != null) {
                        val decryptedData = decryptStringWithPrivateKey(encryptedText)
                        result.success(decryptedData)
                    } else {
                        result.error("INVALID_INPUT", "No encrypted data provided for decryption", null)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun generateKeyPair() {
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setKeySize(2048)
            .build()
        keyPairGenerator.initialize(parameterSpec)
        keyPairGenerator.generateKeyPair()
    }

    private fun getPublicKey(): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey
        return android.util.Base64.encodeToString(publicKey.encoded, android.util.Base64.NO_WRAP)
    }

    private fun encryptStringWithPublicKey(plaintext: String, publicKeyString: String): String {
        val keyBytes = android.util.Base64.decode(publicKeyString, android.util.Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val publicKey: PublicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray())
        return android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.NO_WRAP)
    }

    private fun decryptStringWithPrivateKey(encryptedData: String): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedBytes = cipher.doFinal(android.util.Base64.decode(encryptedData, android.util.Base64.NO_WRAP))
        return String(decryptedBytes)
    }
}
