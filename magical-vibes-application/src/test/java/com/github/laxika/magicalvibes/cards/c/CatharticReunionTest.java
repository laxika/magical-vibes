package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatharticReunionTest extends BaseCardTest {

    @Test
    @DisplayName("Discards two cards as a cost, then draws three cards")
    void discardsTwoThenDrawsThree() {
        CatharticReunion reunion = new CatharticReunion();
        Forest forest = new Forest();
        Island island = new Island();
        Mountain mountain = new Mountain();
        harness.setHand(player1, new ArrayList<>(List.of(reunion, forest, island, mountain)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithDiscards(player1, 0, 0, (java.util.UUID) null, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(reunion.getId(), forest.getId(), island.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(mountain.getId())
                .hasSize(4);
    }

    @Test
    @DisplayName("Cannot be cast without two other cards to discard")
    void cannotCastWithoutTwoCardsToDiscard() {
        harness.setHand(player1, new ArrayList<>(List.of(new CatharticReunion(), new Forest())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 0, (java.util.UUID) null, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard 2");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }
}
