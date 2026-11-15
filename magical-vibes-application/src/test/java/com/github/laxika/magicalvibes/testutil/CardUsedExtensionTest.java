package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.carddata.CardRegistry;
import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void theDeclaredCardsLimitTheOfferedNamesUntilTheClassEnds() throws Exception {
        try (var engine = mockStatic(GameTestEngineContext.class)) {
            engine.when(GameTestEngineContext::get).thenReturn(engineContext);
            when(engineContext.getBean(CardRegistry.class)).thenReturn(registry);

            extension.beforeAll(context(ClassCards.class, "unannotated"));
            assertThat(DeclaredCardNameCatalog.declared()).containsExactly(FirstCard.class);

            extension.beforeEach(context(ClassCards.class, "additionalCards"));
            assertThat(DeclaredCardNameCatalog.declared()).containsExactly(FirstCard.class, SecondCard.class);

            extension.beforeEach(context(ClassCards.class, "unannotated"));
            assertThat(DeclaredCardNameCatalog.declared()).containsExactly(FirstCard.class);

            extension.afterAll(context(ClassCards.class, "unannotated"));
            assertThat(DeclaredCardNameCatalog.declared()).isNull();
        }
    }

    @Test
    void aTestThatDeclaresNoCardsIsOfferedNoCatalogNames() throws Exception {
        try (var engine = mockStatic(GameTestEngineContext.class)) {
            ExtensionContext context = context(MethodCards.class, "unannotated");
            extension.beforeAll(context);
            extension.beforeEach(context);

            assertThat(DeclaredCardNameCatalog.declared()).isEmpty();
            extension.afterAll(context);
        }
    }

    @Test
    void aNestedClassKeepsItsEnclosingClassesDeclarations() throws Exception {
        try (var engine = mockStatic(GameTestEngineContext.class)) {
            engine.when(GameTestEngineContext::get).thenReturn(engineContext);
            when(engineContext.getBean(CardRegistry.class)).thenReturn(registry);

            extension.beforeAll(context(ClassCards.class, "unannotated"));
            ExtensionContext nested = context(ClassCards.Inner.class, "nestedCards");
            extension.beforeAll(nested);
            assertThat(DeclaredCardNameCatalog.declared()).containsExactly(FirstCard.class);

            extension.beforeEach(nested);
            assertThat(DeclaredCardNameCatalog.declared()).containsExactly(FirstCard.class, SecondCard.class);

            extension.afterAll(nested);
            assertThat(DeclaredCardNameCatalog.declared()).isNotNull();
            extension.afterAll(context(ClassCards.class, "unannotated"));
            assertThat(DeclaredCardNameCatalog.declared()).isNull();
        }
    }

    @Test
    void theCatalogOffersOnlyTheDeclaredClassesPrintings() {
        CardPrinting first = new CardPrinting("TST", "1", FirstCard.class.getName(), "FirstCard", false, FirstCard::new);
        CardPrinting reprint = new CardPrinting("TS2", "9", FirstCard.class.getName(), "FirstCard", false, FirstCard::new);
        CardPrinting second = new CardPrinting("TST", "2", SecondCard.class.getName(), "SecondCard", false, SecondCard::new);
        CardCatalog cardCatalog = mock(CardCatalog.class);
        when(cardCatalog.getPrintings(CardSet.values()[0])).thenReturn(List.of(first, reprint, second));
        DeclaredCardNameCatalog catalog = new DeclaredCardNameCatalog(cardCatalog);
        try {
            assertThat(catalog.printings()).containsExactly(first, reprint, second);

            DeclaredCardNameCatalog.declare(List.of(SecondCard.class, Card.class));
            List<CardPrinting> declared = catalog.printings();
            assertThat(declared).containsExactly(second);
            assertThat(catalog.printings()).isSameAs(declared);

            DeclaredCardNameCatalog.declare(List.of(FirstCard.class));
            assertThat(catalog.printings()).containsExactly(first);
        } finally {
            DeclaredCardNameCatalog.clear();
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

        @Nested
        class Inner {
            @CardUsed(SecondCard.class)
            void nestedCards() {
            }
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
