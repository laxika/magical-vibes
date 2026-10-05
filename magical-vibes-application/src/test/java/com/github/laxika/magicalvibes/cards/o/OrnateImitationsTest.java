package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.service.effect.normalfx.ConjureRandomCreatureOfEachManaValueToBattlefieldEffectHandler;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrnateImitations.class, LlanowarElves.class, GrizzlyBears.class, ThayanEvokers.class})
class OrnateImitationsTest extends BaseCardTest {

    @Test
    void conjuresOneRandomCreatureForEachManaValueThroughX() {
        harness.setHand(player1, List.of(new OrnateImitations()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Fix the candidate pool so random selection cannot create a creature that immediately dies.
        var handler = GameTestEngineContext.get().getBean(
                ConjureRandomCreatureOfEachManaValueToBattlefieldEffectHandler.class);
        Object originalCatalog = ReflectionTestUtils.getField(handler, "cardCatalog");
        CardCatalog catalog = mock(CardCatalog.class);
        when(catalog.getPrintings(any(CardSet.class))).thenReturn(List.of(
                new CardPrinting("10E", "274", LlanowarElves.class.getName(), "LlanowarElves", false, LlanowarElves::new),
                new CardPrinting("10E", "268", GrizzlyBears.class.getName(), "GrizzlyBears", false, GrizzlyBears::new)));
        ReflectionTestUtils.setField(handler, "cardCatalog", catalog);
        try {
            harness.castAndResolveSorcery(player1, 0, 2);
        } finally {
            ReflectionTestUtils.setField(handler, "cardCatalog", originalCatalog);
        }

        List<Permanent> conjured = gd.playerBattlefields.get(player1.getId());
        assertThat(conjured).hasSize(2);
        assertThat(conjured).allSatisfy(permanent -> {
            assertThat(permanent.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(permanent.getCard().isToken()).isFalse();
        });
        assertThat(conjured.stream().map(permanent -> permanent.getCard().getManaValue()).toList())
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void conjuringCreatureTriggersConjureAbilities() {
        Permanent evokers = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        harness.setHand(player1, List.of(new OrnateImitations()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        var handler = GameTestEngineContext.get().getBean(
                ConjureRandomCreatureOfEachManaValueToBattlefieldEffectHandler.class);
        Object originalCatalog = ReflectionTestUtils.getField(handler, "cardCatalog");
        CardCatalog catalog = mock(CardCatalog.class);
        when(catalog.getPrintings(any(CardSet.class))).thenReturn(List.of(
                new CardPrinting("10E", "274", LlanowarElves.class.getName(), "LlanowarElves", false, LlanowarElves::new)));
        ReflectionTestUtils.setField(handler, "cardCatalog", catalog);
        try {
            harness.castAndResolveSorcery(player1, 0, 1);
            resolveAllTriggers();
        } finally {
            ReflectionTestUtils.setField(handler, "cardCatalog", originalCatalog);
        }

        Permanent conjured = findPermanent(player1, "Llanowar Elves");
        assertThat(conjured.getCard().isToken()).isFalse();
        assertThat(conjured.getCard().getOwnerId()).isEqualTo(player1.getId());
        assertThat(conjured.isTapped()).isFalse();
        assertThat(conjured.isSummoningSick()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(evokers.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void cannotBeCastWithZeroX() {
        harness.setHand(player1, List.of(new OrnateImitations()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
