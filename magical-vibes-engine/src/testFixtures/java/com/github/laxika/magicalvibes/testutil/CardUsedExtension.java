package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.carddata.CardRegistry;
import com.github.laxika.magicalvibes.model.Card;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Preloads oracle sets by largest shared coverage among the cards declared through
 * {@link CardUsed}, and limits the card names a name prompt offers to those cards
 * ({@link DeclaredCardNameCatalog}).
 */
public final class CardUsedExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        preload(context.getRequiredTestClass());
        declare(context.getRequiredTestClass(), null);
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
        declare(context.getRequiredTestClass(), context.getRequiredTestMethod());
    }

    /** The limit ends with the outermost test class; a nested class hands back to its enclosing one. */
    @Override
    public void afterAll(ExtensionContext context) {
        if (!context.getRequiredTestClass().isAnnotationPresent(Nested.class)) {
            DeclaredCardNameCatalog.clear();
        }
    }

    private static void preload(AnnotatedElement... elements) {
        List<Class<? extends Card>> cardClasses = declaredOn(Arrays.asList(elements));
        if (!cardClasses.isEmpty()) {
            GameTestEngineContext.get()
                    .getBean(CardRegistry.class)
                    .ensureCardDataLoaded(cardClasses);
        }
    }

    /**
     * Offers the names of the cards declared on the running method, its class and, for a
     * {@code @Nested} class, the classes enclosing it — a nested class runs with its enclosing
     * classes' declarations, which {@link #preload} loaded when those classes started.
     */
    private static void declare(Class<?> testClass, Method testMethod) {
        List<AnnotatedElement> elements = new ArrayList<>();
        Class<?> current = testClass;
        elements.add(current);
        while (current.isAnnotationPresent(Nested.class) && current.getEnclosingClass() != null) {
            current = current.getEnclosingClass();
            elements.add(0, current);
        }
        if (testMethod != null) {
            elements.add(testMethod);
        }
        DeclaredCardNameCatalog.declare(declaredOn(elements));
    }

    private static List<Class<? extends Card>> declaredOn(List<AnnotatedElement> elements) {
        return elements.stream()
                .flatMap(element -> AnnotationSupport.findAnnotation(element, CardUsed.class)
                        .map(CardUsed::value)
                        .stream()
                        .flatMap(Arrays::stream))
                .distinct()
                .toList();
    }
}
