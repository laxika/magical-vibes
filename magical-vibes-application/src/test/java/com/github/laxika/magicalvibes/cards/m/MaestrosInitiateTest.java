package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaestrosInitiate.class, Island.class})
class MaestrosInitiateTest extends BaseCardTest {

    @Test
    void activationExilesSourceAndDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        MaestrosInitiate initiate = new MaestrosInitiate();
        harness.setGraveyard(player1, List.of(initiate));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Maestros Initiate");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(initiate);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        MaestrosInitiate initiate = new MaestrosInitiate();
        harness.setGraveyard(player1, List.of(initiate));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Maestros Initiate");
    }

    @Test
    void canPayHybridCostWithRedManaAndDiscardADrawnCard() {
        harness.setHand(player1, List.of());
        Island firstDraw = new Island();
        Island secondDraw = new Island();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        MaestrosInitiate initiate = new MaestrosInitiate();
        harness.setGraveyard(player1, List.of(initiate));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(initiate);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDraw);
    }

    @Test
    void cannotPayHybridCostWithBlackMana() {
        MaestrosInitiate initiate = new MaestrosInitiate();
        harness.setGraveyard(player1, List.of(initiate));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(initiate);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(initiate);
    }

    @Test
    void cannotActivateExiledSourceAgainBeforeResolution() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        MaestrosInitiate initiate = new MaestrosInitiate();
        harness.setGraveyard(player1, List.of(initiate));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(initiate);
    }
}
