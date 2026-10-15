package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gush.class, Island.class, Forest.class})
class GushTest extends BaseCardTest {

    @Test
    void drawsTwoCardsForItsManaCost() {
        harness.setHand(player1, List.of(new Gush()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

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
    void returnsTappedIslandsAsACostBeforeDrawing() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        firstIsland.tap();
        secondIsland.tap();
        harness.setHand(player1, List.of(new Gush()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castWithAlternateCost(player1, 0, List.of(firstIsland.getId(), secondIsland.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island", "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Gush");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island", "Island", "Forest", "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Gush");
    }

    @Test
    void alternateCostReturnsBorrowedIslandToItsOwner() {
        Island borrowedIsland = new Island();
        borrowedIsland.setOwnerId(player2.getId());
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, borrowedIsland);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Gush()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castWithAlternateCost(player1, 0, List.of(borrowed.getId(), own.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(borrowedIsland);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island", "Forest", "Forest");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(borrowedIsland);
        harness.assertInGraveyard(player1, "Gush");
    }

    @Test
    void alternateCostRequiresTwoIslands() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Gush()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void alternateCostRejectsNonIsland() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Gush()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(island.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void alternateCostRequiresIslandsTheCasterControls() {
        Permanent ownIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opposingIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Gush()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(ownIsland.getId(), opposingIsland.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void alternateCostRejectsReturningTheSameIslandTwice() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Gush()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(island.getId(), island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
