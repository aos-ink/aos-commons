package ink.aos.commons.desensitized.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import ink.aos.commons.desensitized.DesensitizedSwitchListen;
import ink.aos.commons.desensitized.DesensitizedSwitchListenRegistry;
import ink.aos.commons.desensitized.annotation.Desensitized;
import ink.aos.commons.desensitized.enums.SensitiveTypeEnum;
import ink.aos.commons.desensitized.utils.DesensitizedUtils;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.Objects;

/**
 * All rights Reserved, Designed By aos.ink
 *
 * @version V1.0
 * @author: lichaohn@163.com
 * @date: 3/3/21
 * @Copyright: 2019 www.aos.ink All rights reserved.
 */
public class DesensitizedSerialize extends JsonSerializer<String> implements
        ContextualSerializer {

    private SensitiveTypeEnum type;

    public DesensitizedSerialize() {
    }

    public DesensitizedSerialize(final SensitiveTypeEnum type) {
        this.type = type;
    }

    @Override
    public void serialize(final String s, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        switch (this.type) {
            case CHINESE_NAME:
                jsonGenerator.writeString(DesensitizedUtils.chineseName(s));
                break;
            case ID_CARD:
                jsonGenerator.writeString(DesensitizedUtils.idCardNum(s));
                break;
            case FIXED_PHONE:
                jsonGenerator.writeString(DesensitizedUtils.fixedPhone(s));
                break;
            case MOBILE_PHONE:
                jsonGenerator.writeString(DesensitizedUtils.mobilePhone(s));
                break;
            case ADDRESS:
                jsonGenerator.writeString(DesensitizedUtils.address(s, 8));
                break;
            case EMAIL:
                jsonGenerator.writeString(DesensitizedUtils.email(s));
                break;
            case BANK_CARD:
                jsonGenerator.writeString(DesensitizedUtils.bankCard(s));
                break;
            case PASSWORD:
                jsonGenerator.writeString(DesensitizedUtils.password(s));
                break;
            case CARNUMBER:
                jsonGenerator.writeString(DesensitizedUtils.carNumber(s));
                break;
            case CNAPS_CODE: {
                jsonGenerator.writeString(DesensitizedUtils.cnapsCode(s));
                break;
            }
        }

    }

    @SneakyThrows
    @Override
    public JsonSerializer<?> createContextual(final SerializerProvider serializerProvider,
                                              final BeanProperty beanProperty) {
        if (beanProperty != null) { // 为空直接跳过
            if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) { // 非 String 类直接跳过
                Desensitized desensitized = beanProperty.getAnnotation(Desensitized.class);
                if (desensitized == null) {
                    desensitized = beanProperty.getContextAnnotation(Desensitized.class);
                }
                if (desensitized != null) { // 如果能得到注解，就将注解的 value 传入 SensitiveInfoSerialize
                    if (desensitized.dynamic()) {
                        DesensitizedSwitchListen desensitizedSwitchListen = DesensitizedSwitchListenRegistry.desensitizedSwitchListen(desensitized.listen());
                        if (desensitizedSwitchListen != null && desensitizedSwitchListen.enable()) {
                            return new DesensitizedSerialize(desensitized.type());
                        }
                    } else {
                        return new DesensitizedSerialize(desensitized.type());
                    }
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.findNullValueSerializer(beanProperty);
    }

}
