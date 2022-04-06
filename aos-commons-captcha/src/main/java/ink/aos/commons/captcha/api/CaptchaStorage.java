package ink.aos.commons.captcha.api;

/**
 * All rights Reserved, Designed By aos.ink
 *
 * @version V1.0
 * @author: lichaohn@163.com
 * @date: 2020-11-06
 * @Copyright: 2019 www.aos.ink All rights reserved.
 */
public interface CaptchaStorage {

    void saveFirstToken(String token, Point point);

    Point getFirstToken(String token);

    Point removeFirstToken(String token);

    void saveSecondToken(String token);

    boolean hasSecondToken(String token);

    void removeSecondToken(String token);
}
