package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.b.BlightKeeper;
import com.github.laxika.magicalvibes.cards.b.BishopOfTheBloodstained;
import com.github.laxika.magicalvibes.cards.j.JadeGuardian;
import com.github.laxika.magicalvibes.cards.f.FathomFleetCutthroat;
import com.github.laxika.magicalvibes.cards.f.FathomFleetFirebrand;
import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimCaptainsCall.class, FathomFleetFirebrand.class, BishopOfTheBloodstained.class,
        FrenziedRaptor.class, JadeGuardian.class, FathomFleetCutthroat.class, BlightKeeper.class, ArcaneAdaptation.class})
class GrimCaptainsCallTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one of each subtype when exactly one of each is in graveyard")
    void returnsAllFourSubtypesAutomatically() {
        harness.setGraveyard(player1, List.of(
                new FathomFleetFirebrand(),  // Pirate
                new BishopOfTheBloodstained(),         // Vampire
                new FrenziedRaptor(),        // Dinosaur
                new JadeGuardian()           // Merfolk
        ));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        // All four creature cards should be in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertInHand(player1, "Fathom Fleet Firebrand");
        harness.assertInHand(player1, "Bishop of the Bloodstained");
        harness.assertInHand(player1, "Frenzied Raptor");
        harness.assertInHand(player1, "Jade Guardian");

        // Graveyard should only contain Grim Captain's Call itself
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName())
                .isEqualTo("Grim Captain's Call");
    }

    @Test
    @DisplayName("Returns only matching subtypes when some are missing")
    void returnsOnlyMatchingSubtypes() {
        harness.setGraveyard(player1, List.of(
                new FathomFleetFirebrand(),  // Pirate
                new FrenziedRaptor()         // Dinosaur
        ));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Only Pirate and Dinosaur should be in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Fathom Fleet Firebrand");
        harness.assertInHand(player1, "Frenzied Raptor");
    }

    @Test
    @DisplayName("Does nothing when graveyard has no matching subtypes")
    void doesNothingWithNoMatchingSubtypes() {
        harness.setGraveyard(player1, List.of(new BlightKeeper()));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Blight Keeper stays in graveyard, nothing returned to hand
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Blight Keeper");
    }

    @Test
    @DisplayName("Does nothing when graveyard is empty")
    void doesNothingWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Prompts for choice when multiple cards of the same subtype exist")
    void promptsForChoiceWithMultipleSameSubtype() {
        harness.setGraveyard(player1, List.of(
                new FathomFleetFirebrand(),  // Pirate
                new FathomFleetCutthroat()   // Pirate
        ));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Should be awaiting a graveyard choice for the Pirate
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        // Choose the first Pirate (index 0)
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Fathom Fleet Firebrand");
        harness.assertInGraveyard(player1, "Fathom Fleet Cutthroat");
    }

    @Test
    @DisplayName("Choice for one subtype does not prevent auto-return of other subtypes")
    void choiceForOneSubtypeDoesNotBlockOthers() {
        harness.setGraveyard(player1, List.of(
                new FathomFleetFirebrand(),  // Pirate
                new FathomFleetCutthroat(),  // Pirate
                new BishopOfTheBloodstained(),         // Vampire
                new FrenziedRaptor()         // Dinosaur
        ));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Later instructions wait for the Pirate choice.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Bishop of the Bloodstained");
        harness.assertInGraveyard(player1, "Frenzied Raptor");

        // Should be awaiting graveyard choice for Pirate
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        // Choose Fathom Fleet Firebrand
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Fathom Fleet Firebrand");
        harness.assertInHand(player1, "Bishop of the Bloodstained");
        harness.assertInHand(player1, "Frenzied Raptor");
    }

    @Test
    @DisplayName("Cannot decline a return when a matching Pirate is available")
    void cannotDeclineRequiredReturn() {
        harness.setGraveyard(player1, List.of(
                new FathomFleetFirebrand(), new FathomFleetCutthroat(), new FrenziedRaptor()));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertInHand(player1, "Fathom Fleet Firebrand");
        harness.assertInHand(player1, "Frenzied Raptor");
        harness.assertInGraveyard(player1, "Fathom Fleet Cutthroat");
    }

    @Test
    @DisplayName("Only returns cards from controller's graveyard, not opponent's")
    void onlyReturnsFromControllersGraveyard() {
        harness.setGraveyard(player1, List.of(new FathomFleetFirebrand()));  // Pirate for player1
        harness.setGraveyard(player2, List.of(
                new BishopOfTheBloodstained(),   // Vampire in opponent's graveyard
                new FrenziedRaptor()   // Dinosaur in opponent's graveyard
        ));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Only Pirate from player1's graveyard should be returned
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName())
                .isEqualTo("Fathom Fleet Firebrand");

        // Opponent's graveyard should be unchanged
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Returns a creature card given Pirate by Arcane Adaptation")
    void returnsCardWithGrantedGraveyardSubtype() {
        harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation())
                .setChosenSubtype(CardSubtype.PIRATE);
        harness.setGraveyard(player1, List.of(new BlightKeeper()));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInHand(player1, "Blight Keeper");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Grim Captain's Call");
    }

    @Test
    @DisplayName("A Pirate Vampire can be chosen for Pirate before processing Vampire")
    void choosesMultiTypedCardForEarlierSubtype() {
        harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation())
                .setChosenSubtype(CardSubtype.PIRATE);
        harness.setGraveyard(player1, List.of(
                new BishopOfTheBloodstained(), new FathomFleetFirebrand()));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Bishop of the Bloodstained");
        harness.assertInGraveyard(player1, "Fathom Fleet Firebrand");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
