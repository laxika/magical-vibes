package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TakeInventory.class, ThermoAlchemist.class})
class TakeInventoryTest extends BaseCardTest {

    private void castTakeInventory() {
        harness.setHand(player1, List.of(new TakeInventory()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
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
        harness.setGraveyard(player1, List.of(new TakeInventory(), new TakeInventory()));
        harness.setGraveyard(player2, List.of(new TakeInventory()));

        castTakeInventory();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The resolving Take Inventory does not count itself")
    void resolvingCopyDoesNotCountItself() {
        harness.setHand(player1, List.of(new TakeInventory(), new TakeInventory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Ignores unrelated graveyard cards and named cards in the library")
    void ignoresOtherNamesAndOtherZones() {
        harness.setGraveyard(player1, List.of(new ThermoAlchemist()));
        TakeInventory drawnCard = new TakeInventory();
        TakeInventory remainingCard = new TakeInventory();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));

        castTakeInventory();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("Counts cards added to the graveyard after casting")
    void countsGraveyardAtResolution() {
        harness.setHand(player1, List.of(new TakeInventory()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);

        harness.setGraveyard(player1, List.of(new TakeInventory(), new TakeInventory()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
