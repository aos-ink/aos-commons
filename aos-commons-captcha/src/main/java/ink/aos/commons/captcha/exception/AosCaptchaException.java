package ink.aos.commons.captcha.exception;

/**
 * All rights Reserved, Designed By aos.ink
 *
 * @version V1.0
 * @author: lichaohn@163.com
 * @date: 2019-07-15
 * @Copyright: 2019 www.aos.ink All rights reserved.
 */
public class AosCaptchaException extends Exception {

    public AosCaptchaException(String message) {
        super(message);
    }

    public AosCaptchaException(String s, Throwable e) {
        super(s, e);
    }
}
