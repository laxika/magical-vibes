package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.carddata.CardRegistry;
import com.github.laxika.magicalvibes.model.Card;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

/**
 * Preloads oracle sets by largest shared coverage among the cards declared through
 * {@link CardUsed}.
 */
public final class CardUsedExtension implements BeforeAllCallback, BeforeEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        preload(context.getRequiredTestClass());
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        // Class declarations were already preloaded by beforeAll. Most methods add
        // nothing, so avoid repeating annotation collection and oracle-set selection.
        if (AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), CardUsed.class).isPresent()) {
            // Keep the combined group so method-specific cards can share a printing
            // set with the class's support cards, just as they did before.
            preload(context.getRequiredTestClass(), context.getRequiredTestMethod());
        }
    }

    private static void preload(AnnotatedElement... elements) {
        List<Class<? extends Card>> cardClasses = Arrays.stream(elements)
                .flatMap(element -> AnnotationSupport.findAnnotation(element, CardUsed.class)
                        .map(CardUsed::value)
                        .stream()
                        .flatMap(Arrays::stream))
                .distinct()
                .toList();
        if (!cardClasses.isEmpty()) {
            GameTestEngineContext.get()
                    .getBean(CardRegistry.class)
                    .ensureCardDataLoaded(cardClasses);
        }
    }
}
