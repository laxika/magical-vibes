package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FranticInventoryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card with no Frantic Inventory in its controller's graveyard")
    void drawsOneCardWithNoCopiesInGraveyard() {
        harness.setHand(player1, List.of(new FranticInventory()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws one card plus one for each matching card in its controller's graveyard")
    void drawsForCopiesInGraveyard() {
        harness.setHand(player1, List.of(new FranticInventory()));
        harness.setGraveyard(player1, List.of(new FranticInventory(), new FranticInventory()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not count matching cards in an opponent's graveyard")
    void ignoresOpponentCopiesInGraveyard() {
        harness.setHand(player1, List.of(new FranticInventory()));
        harness.setGraveyard(player2, List.of(new FranticInventory(), new FranticInventory()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
