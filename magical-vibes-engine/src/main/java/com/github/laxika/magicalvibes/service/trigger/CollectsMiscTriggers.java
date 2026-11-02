package com.github.laxika.magicalvibes.service.trigger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Container annotation for repeatable {@link CollectsMiscTrigger}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CollectsMiscTriggers {
    CollectsMiscTrigger[] value();
}
