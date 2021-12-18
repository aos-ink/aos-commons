package ink.aos.commons.desensitized.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ink.aos.commons.desensitized.DesensitizedSwitchListen;
import ink.aos.commons.desensitized.enums.SensitiveTypeEnum;
import ink.aos.commons.desensitized.jackson.DesensitizedSerialize;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizedSerialize.class)
public @interface Desensitized {
    //    脱敏类型(规则)
    SensitiveTypeEnum type();

    boolean dynamic() default false;

    Class<DesensitizedSwitchListen> listen() default DesensitizedSwitchListen.class;

}