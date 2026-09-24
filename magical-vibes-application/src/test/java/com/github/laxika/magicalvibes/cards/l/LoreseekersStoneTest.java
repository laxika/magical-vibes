package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoreseekersStone.class, Forest.class})
class LoreseekersStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Pays three mana plus the cards in hand and draws three cards")
    void paysBaseCostPlusHandSizeAndDrawsThreeCards() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new LoreseekersStone());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(stone.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate when the hand-size increase cannot be paid")
    void cannotPayBaseCostPlusHandSize() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new LoreseekersStone());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(stone.isTapped()).isFalse();
    }
}
