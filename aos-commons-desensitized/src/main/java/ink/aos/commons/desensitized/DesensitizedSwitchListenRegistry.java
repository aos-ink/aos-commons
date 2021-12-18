package ink.aos.commons.desensitized;

import java.util.HashMap;
import java.util.Map;

/**
 * All rights Reserved, Designed By aos.ink
 *
 * @version V1.0
 * @author: lichaohn@163.com
 * @date: 3/4/21
 * @Copyright: 2019 www.aos.ink All rights reserved.
 */
public class DesensitizedSwitchListenRegistry {

    private static Map<String, DesensitizedSwitchListen> desensitizedSwitchListenMap = new HashMap<>();

    public static void registry(DesensitizedSwitchListen listen) {
        desensitizedSwitchListenMap.put(listen.getClass().getName(), listen);
    }

    public static DesensitizedSwitchListen desensitizedSwitchListen(Class<DesensitizedSwitchListen> listenClass) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        return desensitizedSwitchListen(listenClass.getName());
    }

    public static DesensitizedSwitchListen desensitizedSwitchListen(String className) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        DesensitizedSwitchListen listen = desensitizedSwitchListenMap.get(className);
        if (listen == null) {
            listen = registry(className);
        }
        return listen;
    }

    private static DesensitizedSwitchListen registry(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return registry((Class<DesensitizedSwitchListen>) Class.forName(className));
    }

    private static DesensitizedSwitchListen registry(Class<DesensitizedSwitchListen> listenClass) throws IllegalAccessException, InstantiationException {
        DesensitizedSwitchListen listen = listenClass.newInstance();
        registry(listen);
        return listen;
    }

}
