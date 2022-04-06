package ink.aos.commons.captcha.api;

import java.io.Serializable;

public class Captcha implements Serializable {

    /**
     * 原生图片base64
     */
    private String originalImageBase64;

    /**
     * 滑块图片base64
     */
    private String jigsawImageBase64;

    /**
     * UUID(每次请求的验证码唯一标识)
     */
    private String token;

    public String getOriginalImageBase64() {
        return originalImageBase64;
    }

    public void setOriginalImageBase64(String originalImageBase64) {
        this.originalImageBase64 = originalImageBase64;
    }

    public String getJigsawImageBase64() {
        return jigsawImageBase64;
    }

    public void setJigsawImageBase64(String jigsawImageBase64) {
        this.jigsawImageBase64 = jigsawImageBase64;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
