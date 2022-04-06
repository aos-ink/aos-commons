package ink.aos.test;

import ink.aos.commons.captcha.api.BlockPuzzleCaptcha;
import ink.aos.commons.captcha.api.Captcha;
import ink.aos.commons.captcha.exception.AosCaptchaException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * All rights Reserved, Designed By aos.ink
 *
 * @version V1.0
 * @author: lichaohn@163.com
 * @date: 2020-11-06
 * @Copyright: 2019 www.aos.ink All rights reserved.
 */
public class CaptchaTest {
    private Logger log = LoggerFactory.getLogger(CaptchaTest.class);

    @Test
    public void test1() throws AosCaptchaException, IOException {
        BlockPuzzleCaptcha blockPuzzleCaptcha = new BlockPuzzleCaptcha(new CaptchaStorageImpl());

        for (String s : getResourcesImagesFile("classpath*:defaultImages/jigsaw/original/*.png")) {
            blockPuzzleCaptcha.addOriginalBase64(s);
        }

        for (String s : getResourcesImagesFile("classpath*:defaultImages/jigsaw/slidingBlock/*.png")) {
            blockPuzzleCaptcha.addSlidingBlockBase64(s);
        }

        Captcha captcha = blockPuzzleCaptcha.get();
        log.debug("data:image/jpg;base64," + captcha.getJigsawImageBase64());
        log.debug("data:image/jpg;base64," + captcha.getOriginalImageBase64());
    }

    private static List<String> getResourcesImagesFile(String path) throws IOException {
        //默认提供六张底图
        List<String> imgMap = new ArrayList<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources(path);
        for (Resource resource : resources) {
            String string = Base64Utils.encodeToString(FileCopyUtils.copyToByteArray(resource.getInputStream()));
            imgMap.add(string);
        }
        return imgMap;
    }
}
