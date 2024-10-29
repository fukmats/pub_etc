import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Date;

public class FixedECKeyJWTExample {
    public static void main(String[] args) {
        try {
            // BouncyCastleプロバイダーを追加
            Security.addProvider(new BouncyCastleProvider());

            // 固定の秘密鍵 (例: 16進数形式で指定)
            String fixedPrivateKeyHex = "f57c7ea29b9d13f3a9eabcde1234567890abcdef12345678";
            BigInteger privateKeyValue = new BigInteger(fixedPrivateKeyHex, 16);

            // 楕円曲線のパラメータを取得（例: secp256r1）
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");

            // BouncyCastleでの秘密鍵生成
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, ecSpec);
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            System.out.println("秘密鍵のオブジェクト: " + privateKey);
            System.out.println("秘密鍵のS値 (16進数): " + privateKey.getS().toString(16));

            // 公開鍵を計算
            ECPoint Q = ecSpec.getG().multiply(privateKeyValue);  // 公開鍵ポイントを計算
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(Q, ecSpec);
            ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(pubSpec);

            System.out.println("公開鍵のオブジェクト: " + publicKey);
            System.out.println("公開鍵のX座標: " + publicKey.getW().getAffineX().toString(16));
            System.out.println("公開鍵のY座標: " + publicKey.getW().getAffineY().toString(16));

            // 署名前に秘密鍵と公開鍵が一致するか検証
            boolean preSignatureVerification = verifySignature(privateKey, publicKey);
            System.out.println("事前検証の結果 (署名前): " + preSignatureVerification);

            // JWT作成
            Date now = new Date();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("example.com")                     // iss: 発行者
                .audience("my-audience")                  // aud: 対象
                .expirationTime(new Date(now.getTime() + 1000 * 60 * 10))  // exp: 有効期限 (10分後)
                .issueTime(now)                            // iat: 発行時間
                .claim("nonce", "random-nonce-value")      // nonce: ランダムな値
                .build();

            // 署名のためのシグナーを作成
            JWSSigner signer = new ECDSASigner(privateKey);
            SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID("123")
                    .type(JOSEObjectType.JWT)
                    .build(),
                claimsSet
            );

            // 署名を行う
            signedJWT.sign(signer);

            // 署名直後の検証
            boolean postSignatureVerification = signedJWT.verify(new ECDSAVerifier(publicKey));
            System.out.println("署名後の直後の検証結果: " + postSignatureVerification);

            // 生成されたJWTを文字列に変換
            String jwtString = signedJWT.serialize();
            System.out.println("生成されたJWT: " + jwtString);

            // JWTをデコードし、署名を検証
            SignedJWT decodedJWT = SignedJWT.parse(jwtString);
            boolean isSignatureValid = decodedJWT.verify(new ECDSAVerifier(publicKey));
            System.out.println("最終的な署名の検証結果: " + isSignatureValid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 事前に署名と検証を行うためのメソッド
    public static boolean verifySignature(ECPrivateKey privateKey, ECPublicKey publicKey) {
        try {
            byte[] message = "test message".getBytes();
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(message);
            byte[] signature = ecdsaSign.sign();

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
