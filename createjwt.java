import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jose.jwk.*;
import java.security.KeyFactory;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.math.BigInteger;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.AlgorithmParameters;
import java.util.Date;

public class FixedECKeyJWTExample {
    public static void main(String[] args) {
        try {
            // 固定の秘密鍵 (例: 16進数形式で指定)
            String fixedPrivateKeyHex = "f57c7ea29b9d13f3a9eabcde1234567890abcdef12345678";
            BigInteger privateKeyValue = new BigInteger(fixedPrivateKeyHex, 16);

            // 楕円曲線のパラメータを取得（例: P-256）
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);

            // 秘密鍵を生成
            ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, ecParameterSpec);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            System.out.println("固定の秘密鍵: " + privateKey.getS().toString(16));

            // 公開鍵を計算 (楕円曲線上の点を計算)
            ECPoint ecPoint = ecParameterSpec.getGenerator().multiply(privateKeyValue).normalize();
            ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecPoint, ecParameterSpec);
            ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(publicKeySpec);

            System.out.println("公開鍵のX座標: " + publicKey.getW().getAffineX().toString(16));
            System.out.println("公開鍵のY座標: " + publicKey.getW().getAffineY().toString(16));

            // JWK形式で公開鍵を表示
            ECKey jwk = new ECKey.Builder(Curve.P_256, publicKey)
                .privateKey(privateKey)
                .keyID("123")
                .build();

            System.out.println("JWK形式の公開鍵: " + jwk.toPublicJWK());

            // JWT作成
            // 1. JWTペイロードに含めるクレームを設定
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("1234567890")
                .issuer("example.com")
                .claim("name", "John Doe")
                .claim("admin", true)
                .issueTime(new Date())
                .build();

            // 2. 署名アルゴリズム (ES256) を設定して、JWTを生成
            JWSSigner signer = new ECDSASigner(privateKey);  // 秘密鍵を使って署名を作成
            SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(jwk.getKeyID()).build(),
                claimsSet);

            // 3. 署名を行う
            signedJWT.sign(signer);

            // 4. 生成されたJWTを文字列に変換
            String jwtString = signedJWT.serialize();
            System.out.println("生成されたJWT: " + jwtString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
