package ink.aos.test;

import ink.aos.commons.captcha.api.CaptchaStorage;
import ink.aos.commons.captcha.api.Point;

/**
 * All rights Reserved, Designed By aos.ink
 *
 * @version V1.0
 * @author: lichaohn@163.com
 * @date: 2020-11-06
 * @Copyright: 2019 www.aos.ink All rights reserved.
 */
public class CaptchaStorageImpl implements CaptchaStorage {

    @Override
    public void saveFirstToken(String token, Point point) {

    }

    @Override
    public Point getFirstToken(String token) {
        return null;
    }

    @Override
    public Point removeFirstToken(String token) {
        return null;
    }

    @Override
    public void saveSecondToken(String token) {

    }

    @Override
    public boolean hasSecondToken(String token) {
        return false;
    }

    @Override
    public void removeSecondToken(String token) {

    }
}
