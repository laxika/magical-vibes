package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PotholeMole.class, Forest.class})
class PotholeMoleTest extends BaseCardTest {

    private void castAndResolveToMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PotholeMole()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("ETB mills three cards then prompts may return land")
    void etbMillsThenMayPrompt() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveToMay();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertOnBattlefield(player1, "Pothole Mole");
    }

    @Test
    @DisplayName("Accepting may returns a milled land to hand")
    void acceptingMayReturnsMilledLand() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining may leaves milled cards in graveyard")
    void decliningMayLeavesCardsInGraveyard() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot choose a nonland from graveyard")
    void cannotChooseNonland() {
        harness.setGraveyard(player1, List.of(new PotholeMole(), new Forest()));
        harness.setLibrary(player1, List.of(new PotholeMole(), new PotholeMole(), new PotholeMole()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    void canReturnLandAlreadyInGraveyardWhenNoLandIsMilled() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new PotholeMole(), new PotholeMole(), new PotholeMole()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertNotInHand(player2, "Forest");
    }

    @Test
    void shortLibraryStillAllowsReturningMilledLand() {
        harness.setLibrary(player1, List.of(new PotholeMole(), new Forest()));

        castAndResolveToMay();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void emptyLibraryStillAllowsReturningExistingLand() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void acceptingWithNoLandDoesNotRequireGraveyardChoice() {
        harness.setLibrary(player1, List.of(new PotholeMole(), new PotholeMole(), new PotholeMole()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
