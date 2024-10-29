import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jose.jwk.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.Date;

public class FixedECKeyJWTExample {
    public static void main(String[] args) {
        try {
            // 固定の秘密鍵 (例: 16進数形式で指定)
            String fixedPrivateKeyHex = "f57c7ea29b9d13f3a9eabcde1234567890abcdef12345678";
            BigInteger privateKeyValue = new BigInteger(fixedPrivateKeyHex, 16);

            // 楕円曲線の鍵ペアジェネレーターを初期化（P-256）
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            keyPairGenerator.initialize(ecSpec);

            // キーペアを生成
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // ECPrivateKeySpecを使って固定の秘密鍵を設定
            ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, ((ECPublicKey) keyPair.getPublic()).getParams());
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            // 公開鍵はKeyPairのものを使用する（公開鍵は固定された秘密鍵に基づく）
            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

            // 秘密鍵と公開鍵を表示
            System.out.println("秘密鍵のS値 (16進数): " + privateKey.getS().toString(16));
            System.out.println("公開鍵のX座標: " + publicKey.getW().getAffineX().toString(16));
            System.out.println("公開鍵のY座標: " + publicKey.getW().getAffineY().toString(16));

            // 署名の前に検証 (公開鍵と秘密鍵が正しいか確認する)
            boolean preSignatureVerification = verifySignature(privateKey, publicKey);
            System.out.println("事前検証の結果 (署名前): " + preSignatureVerification);

            // 現在の時間
            Date now = new Date();

            // JWT作成
            // 1. JWTペイロードに含めるクレームを設定 (iss, aud, exp, iat, nonce)
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("example.com")                     // iss: 発行者
                .audience("my-audience")                  // aud: 対象
                .expirationTime(new Date(now.getTime() + 1000 * 60 * 10))  // exp: 有効期限 (10分後)
                .issueTime(now)                            // iat: 発行時間
                .claim("nonce", "random-nonce-value")      // nonce: ランダムな値
                .build();

            // 2. ヘッダーを指定してJWTを生成 (kid, typ, alg)
            JWSSigner signer = new ECDSASigner(privateKey);  // 秘密鍵を使って署名を作成
            SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID("123")                  // kid
                    .type(JOSEObjectType.JWT)       // typ
                    .build(),
                claimsSet);

            // 3. 署名を行う
            signedJWT.sign(signer);

            // JWT署名の直後に検証
            boolean postSignatureVerification = signedJWT.verify(new ECDSAVerifier(publicKey));
            System.out.println("署名後の直後の検証結果: " + postSignatureVerification);

            // 4. 生成されたJWTを文字列に変換
            String jwtString = signedJWT.serialize();
            System.out.println("生成されたJWT: " + jwtString);

            // JWTをデコードする
            // 1. デコードするためにJWT文字列を再度SignedJWTオブジェクトとして読み込む
            SignedJWT decodedJWT = SignedJWT.parse(jwtString);

            // 2. JWTのペイロードを表示
            System.out.println("デコードされたペイロード: " + decodedJWT.getJWTClaimsSet().toJSONObject());

            // 3. デコード後の署名の検証 (公開鍵を使用)
            JWSVerifier verifier = new ECDSAVerifier(publicKey);
            boolean isSignatureValid = decodedJWT.verify(verifier);
            System.out.println("最終的な署名の検証結果: " + isSignatureValid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 事前に署名と検証を行うためのメソッド
    public static boolean verifySignature(ECPrivateKey privateKey, ECPublicKey publicKey) {
        try {
            // テストメッセージを作成
            byte[] message = "test message".getBytes();

            // 署名作成
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(message);
            byte[] signature = ecdsaSign.sign();

            // 署名の検証
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(message);
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jose.jwk.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.Date;

public class FixedECKeyJWTExample {
    public static void main(String[] args) {
        try {
            // 固定の秘密鍵 (例: 16進数形式で指定)
            String fixedPrivateKeyHex = "f57c7ea29b9d13f3a9eabcde1234567890abcdef12345678";
            BigInteger privateKeyValue = new BigInteger(fixedPrivateKeyHex, 16);

            // 楕円曲線の鍵ペアジェネレーターを初期化（P-256）
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            keyPairGenerator.initialize(ecSpec);

            // キーペアを生成
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // ECPrivateKeySpecを使って固定の秘密鍵を設定
            ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, ((ECPublicKey) keyPair.getPublic()).getParams());
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            // 公開鍵はKeyPairのものを使用する（公開鍵は固定された秘密鍵に基づく）
            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

            // 秘密鍵と公開鍵を表示
            System.out.println("秘密鍵のS値 (16進数): " + privateKey.getS().toString(16));
            System.out.println("公開鍵のX座標: " + publicKey.getW().getAffineX().toString(16));
            System.out.println("公開鍵のY座標: " + publicKey.getW().getAffineY().toString(16));

            // 署名の前に検証 (公開鍵と秘密鍵が正しいか確認する)
            boolean preSignatureVerification = verifySignature(privateKey, publicKey);
            System.out.println("事前検証の結果 (署名前): " + preSignatureVerification);

            // 現在の時間
            Date now = new Date();

            // JWT作成
            // 1. JWTペイロードに含めるクレームを設定 (iss, aud, exp, iat, nonce)
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("example.com")                     // iss: 発行者
                .audience("my-audience")                  // aud: 対象
                .expirationTime(new Date(now.getTime() + 1000 * 60 * 10))  // exp: 有効期限 (10分後)
                .issueTime(now)                            // iat: 発行時間
                .claim("nonce", "random-nonce-value")      // nonce: ランダムな値
                .build();

            // 2. ヘッダーを指定してJWTを生成 (kid, typ, alg)
            JWSSigner signer = new ECDSASigner(privateKey);  // 秘密鍵を使って署名を作成
            SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID("123")                  // kid
                    .type(JOSEObjectType.JWT)       // typ
                    .build(),
                claimsSet);

            // 3. 署名を行う
            signedJWT.sign(signer);

            // JWT署名の直後に検証
            boolean postSignatureVerification = signedJWT.verify(new ECDSAVerifier(publicKey));
            System.out.println("署名後の直後の検証結果: " + postSignatureVerification);

            // 4. 生成されたJWTを文字列に変換
            String jwtString = signedJWT.serialize();
            System.out.println("生成されたJWT: " + jwtString);

            // JWTをデコードする
            // 1. デコードするためにJWT文字列を再度SignedJWTオブジェクトとして読み込む
            SignedJWT decodedJWT = SignedJWT.parse(jwtString);

            // 2. JWTのペイロードを表示
            System.out.println("デコードされたペイロード: " + decodedJWT.getJWTClaimsSet().toJSONObject());

            // 3. デコード後の署名の検証 (公開鍵を使用)
            JWSVerifier verifier = new ECDSAVerifier(publicKey);
            boolean isSignatureValid = decodedJWT.verify(verifier);
            System.out.println("最終的な署名の検証結果: " + isSignatureValid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 事前に署名と検証を行うためのメソッド
    public static boolean verifySignature(ECPrivateKey privateKey, ECPublicKey publicKey) {
        try {
            // テストメッセージを作成
            byte[] message = "test message".getBytes();

            // 署名作成
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(message);
            byte[] signature = ecdsaSign.sign();

            // 署名の検証
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(message);
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
