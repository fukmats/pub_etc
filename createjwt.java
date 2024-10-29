import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jose.jwk.*;
import java.math.BigInteger;
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

            // 楕円曲線の秘密鍵と公開鍵を生成 (固定された秘密鍵を使用)
            ECKey ecJWK = new ECKey.Builder(Curve.P_256, privateKeyValue)  // カーブと秘密鍵の値を指定
                .keyUse(KeyUse.SIGNATURE)  // 署名用として使用
                .keyID("123")              // 任意のキーID
                .build();

            // 秘密鍵の取得
            ECPrivateKey privateKey = ecJWK.toECPrivateKey();
            System.out.println("秘密鍵のオブジェクト: " + privateKey);

            // 秘密鍵のS値 (楕円曲線の秘密値) を取得して16進数形式で表示
            BigInteger s = privateKey.getS();
            System.out.println("秘密鍵の値 (16進数): " + s.toString(16));

            // Base64エンコード形式での表示
            System.out.println("秘密鍵の値 (Base64): " + Base64.getEncoder().encodeToString(s.toByteArray()));

            // 公開鍵の取得
            ECPublicKey publicKey = ecJWK.toECPublicKey();
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
                    .keyID(ecJWK.getKeyID())        // kid
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
