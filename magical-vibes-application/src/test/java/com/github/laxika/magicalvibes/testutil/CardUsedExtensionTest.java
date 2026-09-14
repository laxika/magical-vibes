package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.carddata.CardRegistry;
import com.github.laxika.magicalvibes.model.Card;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CardUsedExtensionTest {

    private final CardUsedExtension extension = new CardUsedExtension();
    private final CardRegistry registry = mock(CardRegistry.class);
    private final AnnotationConfigApplicationContext engineContext = mock(AnnotationConfigApplicationContext.class);

    @Test
    void classDeclarationsAreNotPreloadedAgainBeforeUnannotatedMethods() throws Exception {
        try (var engine = mockStatic(GameTestEngineContext.class)) {
            engine.when(GameTestEngineContext::get).thenReturn(engineContext);
            when(engineContext.getBean(CardRegistry.class)).thenReturn(registry);

            ExtensionContext context = context(ClassCards.class, "unannotated");
            extension.beforeAll(context);
            extension.beforeEach(context);
            extension.beforeEach(context);

            verify(registry).ensureCardDataLoaded(List.of(FirstCard.class));
            verifyNoMoreInteractions(registry);
            engine.verify(GameTestEngineContext::get);
        }
    }

    @Test
    void methodDeclarationsKeepCombinedSetSelectionAndRemoveDuplicates() throws Exception {
        try (var engine = mockStatic(GameTestEngineContext.class)) {
            engine.when(GameTestEngineContext::get).thenReturn(engineContext);
            when(engineContext.getBean(CardRegistry.class)).thenReturn(registry);

            ExtensionContext context = context(ClassCards.class, "additionalCards");
            extension.beforeAll(context);
            extension.beforeEach(context);

            verify(registry).ensureCardDataLoaded(List.of(FirstCard.class));
            verify(registry).ensureCardDataLoaded(List.of(FirstCard.class, SecondCard.class));
            verifyNoMoreInteractions(registry);
        }
    }

    @Test
    void methodDeclarationsWorkWithoutAClassDeclaration() throws Exception {
        try (var engine = mockStatic(GameTestEngineContext.class)) {
            engine.when(GameTestEngineContext::get).thenReturn(engineContext);
            when(engineContext.getBean(CardRegistry.class)).thenReturn(registry);

            ExtensionContext context = context(MethodCards.class, "declaredCards");
            extension.beforeAll(context);
            engine.verifyNoInteractions();
            extension.beforeEach(context);

            verify(registry).ensureCardDataLoaded(List.of(SecondCard.class));
            verifyNoMoreInteractions(registry);
        }
    }

    @Test
    void unannotatedTestsDoNotInitializeTheEngine() throws Exception {
        try (var engine = mockStatic(GameTestEngineContext.class)) {
            ExtensionContext context = context(MethodCards.class, "unannotated");
            extension.beforeAll(context);
            extension.beforeEach(context);

            engine.verifyNoInteractions();
        }
    }

    private static ExtensionContext context(Class<?> testClass, String methodName) throws Exception {
        ExtensionContext context = mock(ExtensionContext.class);
        doReturn(testClass).when(context).getRequiredTestClass();
        when(context.getRequiredTestMethod()).thenReturn(testClass.getDeclaredMethod(methodName));
        return context;
    }

    @CardUsed(FirstCard.class)
    static class ClassCards {
        void unannotated() {
        }

        @CardUsed({SecondCard.class, FirstCard.class, SecondCard.class})
        void additionalCards() {
        }
    }

    static class MethodCards {
        @CardUsed(SecondCard.class)
        void declaredCards() {
        }

        void unannotated() {
        }
    }

    static class FirstCard extends Card {
    }

    static class SecondCard extends Card {
    }
}
