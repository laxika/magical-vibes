package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FranticInventory.class, Island.class})
class FranticInventoryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card with no Frantic Inventory in its controller's graveyard")
    void drawsOneCardWithNoCopiesInGraveyard() {
        harness.setHand(player1, List.of(new FranticInventory()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws one card plus one for each matching card in its controller's graveyard")
    void drawsForCopiesInGraveyard() {
        harness.setHand(player1, List.of(new FranticInventory()));
        harness.setGraveyard(player1, List.of(new FranticInventory(), new FranticInventory()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not count matching cards in an opponent's graveyard")
    void ignoresOpponentCopiesInGraveyard() {
        harness.setHand(player1, List.of(new FranticInventory()));
        harness.setGraveyard(player2, List.of(new FranticInventory(), new FranticInventory()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts only matching names in a mixed graveyard")
    void ignoresUnrelatedCardsInGraveyard() {
        harness.setHand(player1, List.of(new FranticInventory()));
        harness.setGraveyard(player1, List.of(new Island(), new FranticInventory(), new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Frantic Inventory");
    }

    @Test
    @DisplayName("A later resolution counts an earlier resolved copy, but not itself")
    void countsCopiesThatResolveWhileOnStack() {
        harness.setHand(player1, List.of(new FranticInventory(), new FranticInventory()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
