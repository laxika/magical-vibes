package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaticInsight.class, Mountain.class, FieryImpulse.class})
class MagmaticInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a land as the additional cost, then draws two cards")
    void discardsLandThenDrawsTwo() {
        harness.setHand(player1, List.of(new MagmaticInsight(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        // Started with 2 cards, cast one, discarded one, then drew two.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot discard a nonland card to pay the cost")
    void cannotDiscardNonlandCard() {
        harness.setHand(player1, List.of(new MagmaticInsight(), new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast with no land card in hand")
    void cannotCastWithoutLand() {
        harness.setHand(player1, List.of(new MagmaticInsight(), new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Pays the discard cost before resolution and draws only for the caster")
    void paysDiscardBeforeResolution() {
        harness.setHand(player1, List.of(new MagmaticInsight(), new Mountain()));
        harness.setHand(player2, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new FieryImpulse(), new Mountain(), new MagmaticInsight()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithDiscard(player1, 0, 1);

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Fiery Impulse");
        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Magmatic Insight");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can discard a land before the spell in hand")
    void discardsLandBeforeSpellInHand() {
        harness.setHand(player1, List.of(new Mountain(), new MagmaticInsight(), new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithDiscard(player1, 1, 0);

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInHand(player1, "Fiery Impulse");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Magmatic Insight");
    }

    @Test
    @DisplayName("Cannot omit the additional cost even with a land in hand")
    void cannotOmitDiscardCost() {
        harness.setHand(player1, List.of(new MagmaticInsight(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Magmatic Insight");
        harness.assertInHand(player1, "Mountain");
        harness.assertNotInGraveyard(player1, "Mountain");
        assertThat(gd.stack).isEmpty();
    }
}
