package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GushTest extends BaseCardTest {

    @Test
    void drawsTwoCardsForItsManaCost() {
        harness.setHand(player1, List.of(new Gush()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island", "Island");
        harness.assertInGraveyard(player1, "Gush");
    }

    @Test
    void returnsTwoIslandsAndDrawsTwoCardsForAlternateCost() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Gush()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        harness.castWithAlternateCost(player1, 0, List.of(firstIsland.getId(), secondIsland.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island", "Island", "Island", "Island");
        harness.assertInGraveyard(player1, "Gush");
    }

    @Test
    void alternateCostRequiresTwoIslands() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Gush()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
