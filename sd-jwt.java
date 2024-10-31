import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class DisclosureGenerator {
    public static void main(String[] args) throws Exception {
        // HashMapの定義
        HashMap<String, String> user = new HashMap<>();
        user.put("nickname", "Taka");
        user.put("age", "25");
        user.put("email", "taka@example.com");

        List<String> digests = new ArrayList<>();
        
        // 各値に対してDisclosureとダイジェストを生成
        for (Map.Entry<String, String> entry : user.entrySet()) {
            // ランダムなsoltを生成（16バイト）
            byte[] salt = generateRandomSalt();
            String saltBase64 = base64UrlEncode(salt);
            
            // Disclosureの配列を作成
            String[] disclosureArray = new String[]{
                saltBase64,
                entry.getKey(),
                entry.getValue()
            };
            
            // 配列をJSON文字列に変換
            String jsonArray = arrayToJsonString(disclosureArray);
            
            // UTF-8のバイト配列に変換
            byte[] jsonBytes = jsonArray.getBytes(StandardCharsets.UTF_8);
            
            // Base64URLエンコード
            String disclosure = base64UrlEncode(jsonBytes);
            System.out.println("Disclosure: " + disclosure);
            
            // ダイジェストを計算
            String digest = calculateDigest(disclosure);
            digests.add(digest);
            System.out.println("Digest: " + digest);
        }
        
        // すべてのダイジェストをリストとして出力
        System.out.println("\nAll Digests:");
        System.out.println(arrayToJsonString(digests.toArray(new String[0])));
    }
    
    // ランダムなsaltを生成
    private static byte[] generateRandomSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
    
    // Base64URLエンコード
    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
    
    // 配列をJSON文字列に変換
    private static String arrayToJsonString(String[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(array[i].replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
    
    // ダイジェストを計算（SHA-256ベース）
    private static String calculateDigest(String disclosure) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(disclosure.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(hash);
    }
}
