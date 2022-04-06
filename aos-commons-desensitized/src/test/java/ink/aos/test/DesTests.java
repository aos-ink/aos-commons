package ink.aos.test;

import ink.aos.commons.desensitized.utils.DesensitizedUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DesTests {

    @Test
    public void test1() {
        log.debug(DesensitizedUtils.mobilePhone("15110017979"));
    }

    @Test
    public void test2() {
        log.debug(DesensitizedUtils.idCardNum("123456789012345678"));
    }

}
