package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.carddata.CardRegistry;
import com.github.laxika.magicalvibes.model.Card;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

/** Preloads oracle sets declared through {@link CardUsed}. */
public final class CardUsedExtension implements BeforeAllCallback, BeforeEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        preload(context.getRequiredTestClass());
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        preload(context.getRequiredTestMethod());
    }

    private static void preload(AnnotatedElement element) {
        AnnotationSupport.findAnnotation(element, CardUsed.class)
                .map(CardUsed::value)
                .stream()
                .flatMap(Arrays::stream)
                .distinct()
                .forEach(CardUsedExtension::preload);
    }

    private static void preload(Class<? extends Card> cardClass) {
        GameTestEngineContext.get()
                .getBean(CardRegistry.class)
                .ensureCardDataLoaded(cardClass);
    }
}
