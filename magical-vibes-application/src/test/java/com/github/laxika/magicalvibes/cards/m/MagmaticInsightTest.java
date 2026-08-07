package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.setHand(player1, List.of(new MagmaticInsight(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast with no land card in hand")
    void cannotCastWithoutLand() {
        harness.setHand(player1, List.of(new MagmaticInsight(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
