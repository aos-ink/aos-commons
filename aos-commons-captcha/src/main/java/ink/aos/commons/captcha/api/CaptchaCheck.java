package ink.aos.commons.captcha.api;

import java.io.Serializable;

public class CaptchaCheck implements Serializable {

    /**
     * 滑块点选坐标
     */
    private Point point;

    /**
     * UUID(每次请求的验证码唯一标识)
     */
    private String token;

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
