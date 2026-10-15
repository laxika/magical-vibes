package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AimHigh;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
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

@CardUsed({GrappleWithThePast.class, Forest.class, YoungWolf.class, AimHigh.class})
class GrappleWithThePastTest extends BaseCardTest {

    private void castAndResolveToMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrappleWithThePast()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve spell → mill, then may prompt
    }

    @Test
    @DisplayName("Resolves by milling three then prompting may return creature or land")
    void millsThenMayPrompt() {
        Forest f1 = new Forest();
        Forest f2 = new Forest();
        Forest f3 = new Forest();
        harness.setLibrary(player1, List.of(f1, f2, f3));

        castAndResolveToMay();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        // CR 608.2n: the spell is put into its owner's graveyard only as the final part of its
        // resolution, so while the "you may return" choice is open only the 3 milled cards are there.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may returns a milled creature to hand")
    void acceptingMayReturnsMilledCreature() {
        YoungWolf wolf = new YoungWolf();
        harness.setLibrary(player1, List.of(wolf, new Forest(), new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        // The creature is at graveyard index 0.
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Young Wolf");
    }

    @Test
    @DisplayName("Accepting may returns a milled land to hand")
    void acceptingMayReturnsMilledLand() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new YoungWolf(), new YoungWolf()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining may leaves milled cards in graveyard")
    void decliningMayLeavesCardsInGraveyard() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest(), new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4); // 3 milled + Grapple
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot choose a non-creature non-land from graveyard")
    void cannotChooseNonCreatureNonLand() {
        harness.setGraveyard(player1, List.of(new AimHigh(), new Forest()));
        harness.setLibrary(player1, List.of(new AimHigh(), new AimHigh(), new AimHigh()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        // AimHigh is first in graveyard but illegal; choosing it must fail
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Returns a creature already in the graveyard, not only a milled card")
    void returnsPreviouslyBuriedCreature() {
        YoungWolf wolf = new YoungWolf();
        harness.setGraveyard(player1, List.of(wolf));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new AimHigh(), new AimHigh(), new AimHigh(), new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wolf);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mills the available cards from a library smaller than three")
    void shortLibraryStillAllowsReturn() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grapple with the Past");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library does not prevent returning an existing land")
    void emptyLibraryStillAllowsReturn() {
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of());

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Grapple with the Past");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Resolves without a return when neither graveyard nor milled cards contain a creature or land")
    void noEligibleCardsStillCompletesResolution() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new YoungWolf(), new Forest()));
        harness.setLibrary(player1, List.of(new AimHigh(), new AimHigh(), new AimHigh()));

        castAndResolveToMay();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
