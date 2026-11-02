package com.github.laxika.magicalvibes.service.trigger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Container annotation for repeatable {@link CollectsLibraryTrigger}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CollectsLibraryTriggers {
    CollectsLibraryTrigger[] value();
}
