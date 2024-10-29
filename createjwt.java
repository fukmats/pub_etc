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

            // 楕円曲線のパラメータを取得（例: P-256）
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);

            // 秘密鍵を生成
            ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, ecParameterSpec);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            // 公開鍵を計算
            ECPoint ecPoint = ecParameterSpec.getGenerator().multiply(privateKeyValue).normalize();
            ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecPoint, ecParameterSpec);
            ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(publicKeySpec);

            System.out.println("秘密鍵のオブジェクト: " + privateKey);
            System.out.println("公開鍵のオブジェクト: " + publicKey);

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

            // 4. 生成されたJWTを文字列に変換
            String jwtString = signedJWT.serialize();
            System.out.println("生成されたJWT: " + jwtString);

            // JWTをデコードする
            // 1. デコードするためにJWT文字列を再度SignedJWTオブジェクトとして読み込む
            SignedJWT decodedJWT = SignedJWT.parse(jwtString);

            // 2. JWTのペイロードを表示
            System.out.println("デコードされたペイロード: " + decodedJWT.getJWTClaimsSet().toJSONObject());

            // 3. 署名の検証 (公開鍵を使用)
            JWSVerifier verifier = new ECDSAVerifier(publicKey);
            boolean isSignatureValid = decodedJWT.verify(verifier);
            System.out.println("署名の検証結果: " + isSignatureValid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
