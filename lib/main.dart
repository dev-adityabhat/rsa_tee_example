import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'RSA TEE Example',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const RSAExample(),
    );
  }
}

class RSAExample extends StatefulWidget {
  const RSAExample({Key? key}) : super(key: key);

  @override
  State<RSAExample> createState() => _RSAExampleState();
}

class _RSAExampleState extends State<RSAExample> {
  static const platform = MethodChannel('flutter/rsa_tee');

  String _publicKey = '';
  String _encryptedData = '';
  String _decryptedData = '';
  String _inputText = '';

  Future<void> generateKeyPair() async {
    try {
      await platform.invokeMethod('generateKeyPair');
      setState(() {
        _publicKey = "Key Pair Generated!";
      });
    } catch (e) {
      setState(() {
        _publicKey = "Error generating key pair: $e";
      });
    }
  }

  Future<void> encryptData() async {
    try {
      final result = await platform.invokeMethod('encrypt', {'text': _inputText});
      setState(() {
        _publicKey = result['publicKey'];
        _encryptedData = result['encryptedData'];
        _decryptedData = '';
      });
    } catch (e) {
      setState(() {
        _encryptedData = "Error encrypting data: $e";
      });
    }
  }

  Future<void> decryptData() async {
    try {
      final result = await platform.invokeMethod('decrypt', {'encryptedData': _encryptedData});
      setState(() {
        _decryptedData = result;
      });
    } catch (e) {
      setState(() {
        _decryptedData = "Error decrypting data: $e";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('RSA TEE Example')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            TextField(
              onChanged: (value) {
                _inputText = value;
              },
              decoration: const InputDecoration(labelText: 'Input Text'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(onPressed: generateKeyPair, child: const Text('Generate Key Pair')),
            const SizedBox(height: 16),
            ElevatedButton(onPressed: encryptData, child: const Text('Encrypt')),
            const SizedBox(height: 16),
            ElevatedButton(onPressed: decryptData, child: const Text('Decrypt')),
            const SizedBox(height: 16),
            Text('Public Key: $_publicKey'),
            const SizedBox(height: 16),
            Text('Encrypted Data: $_encryptedData'),
            const SizedBox(height: 16),
            Text('Decrypted Data: $_decryptedData'),
          ],
        ),
      ),
    );
  }
}
