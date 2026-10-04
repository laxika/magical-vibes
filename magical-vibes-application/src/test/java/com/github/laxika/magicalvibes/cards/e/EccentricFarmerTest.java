package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({EccentricFarmer.class, Forest.class})
class EccentricFarmerTest extends BaseCardTest {

    private void castAndResolveToMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EccentricFarmer()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB (mill, then may prompt)
    }

    @Test
    @DisplayName("ETB mills three cards then prompts may return land")
    void etbMillsThenMayPrompt() {
        Forest f1 = new Forest();
        Forest f2 = new Forest();
        Forest f3 = new Forest();
        harness.setLibrary(player1, List.of(f1, f2, f3));

        castAndResolveToMay();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertOnBattlefield(player1, "Eccentric Farmer");
    }

    @Test
    @DisplayName("Accepting may returns a milled land to hand")
    void acceptingMayReturnsMilledLand() {
        Forest f1 = new Forest();
        Forest f2 = new Forest();
        Forest f3 = new Forest();
        harness.setLibrary(player1, List.of(f1, f2, f3));

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
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest(), new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot choose a nonland from graveyard")
    void cannotChooseNonland() {
        harness.setGraveyard(player1, List.of(new EccentricFarmer(), new Forest()));
        harness.setLibrary(player1, List.of(new EccentricFarmer(), new EccentricFarmer(), new EccentricFarmer()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Can return a land already in the graveyard with an empty library")
    void returnsExistingLandWithEmptyLibrary() {
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of());

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mills only available cards and leaves the opponent's zones unchanged")
    void shortLibraryDoesNotAffectOpponent() {
        Forest land = new Forest();
        EccentricFarmer nonland = new EccentricFarmer();
        Forest opponentLand = new Forest();
        harness.setLibrary(player1, List.of(nonland, land));
        harness.setLibrary(player2, List.of(opponentLand));
        harness.setGraveyard(player2, List.of(new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With no land available, accepting the optional return finishes without a card choice")
    void noLandAvailableFinishesResolution() {
        harness.setLibrary(player1, List.of(new EccentricFarmer(), new EccentricFarmer(), new EccentricFarmer()));
        harness.setGraveyard(player2, List.of(new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
