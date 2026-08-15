package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TakeInventoryTest extends BaseCardTest {

    private void castTakeInventory() {
        harness.setHand(player1, List.of(new TakeInventory()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws one card with no Take Inventory in your graveyard")
    void drawsOneWithNoNamedCardsInGraveyard() {
        castTakeInventory();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts Take Inventory only in the controller's graveyard")
    void countsNamedCardsOnlyInControllerGraveyard() {
        gd.playerGraveyards.get(player1.getId()).add(new TakeInventory());
        gd.playerGraveyards.get(player1.getId()).add(new TakeInventory());
        gd.playerGraveyards.get(player2.getId()).add(new TakeInventory());

        castTakeInventory();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The resolving Take Inventory does not count itself")
    void resolvingCopyDoesNotCountItself() {
        harness.setHand(player1, List.of(new TakeInventory(), new TakeInventory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }
}
