package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.carddata.CardRegistry;
import com.github.laxika.magicalvibes.carddata.DeckLegalityRegistry;
import com.github.laxika.magicalvibes.carddata.store.SetDataStore;
import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class HeadlessSimulationContextTest {

    @Test
    void sharedCardDataIsReusedInsteadOfLoadedAgain() {
        AnnotationConfigApplicationContext live = GameTestEngineContext.get();
        CardRegistry cardRegistry = live.getBean(CardRegistry.class);
        RandomDeckGenerator randomDeckGenerator = live.getBean(RandomDeckGenerator.class);
        DeckLegalityRegistry deckLegalityRegistry = live.getBean(DeckLegalityRegistry.class);

        try (AnnotationConfigApplicationContext headless = HeadlessSimulationContext.build(
                new HeadlessSimulationContext.SharedCardData(cardRegistry, randomDeckGenerator,
                        deckLegalityRegistry))) {
            assertThat(headless.getBean(CardCatalog.class)).isSameAs(cardRegistry);
            assertThat(headless.getBean(RandomDeckGenerator.class)).isSameAs(randomDeckGenerator);
            assertThat(headless.getBean(DeckLegalityRegistry.class)).isSameAs(deckLegalityRegistry);
            assertThat(headless.getBeansOfType(SetDataStore.class)).isEmpty();
        }
    }
}
