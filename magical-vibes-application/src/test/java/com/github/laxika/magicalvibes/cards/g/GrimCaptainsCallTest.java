package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.FathomFleetCutthroat;
import com.github.laxika.magicalvibes.cards.f.FathomFleetFirebrand;
import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReturnOneOfEachSubtypeFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrimCaptainsCallTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has spell effect that returns one of each subtype from graveyard to hand")
    void hasCorrectEffect() {
        GrimCaptainsCall card = new GrimCaptainsCall();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ReturnOneOfEachSubtypeFromGraveyardToHandEffect.class);

        ReturnOneOfEachSubtypeFromGraveyardToHandEffect effect =
                (ReturnOneOfEachSubtypeFromGraveyardToHandEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.subtypes()).containsExactly(
                CardSubtype.PIRATE, CardSubtype.VAMPIRE, CardSubtype.DINOSAUR, CardSubtype.MERFOLK);
    }

    // ===== Resolution: all four subtypes present with single matches =====

    @Test
    @DisplayName("Returns one of each subtype when exactly one of each is in graveyard")
    void returnsAllFourSubtypesAutomatically() {
        harness.setGraveyard(player1, List.of(
                new FathomFleetFirebrand(),  // Pirate
                new BaronyVampire(),         // Vampire
                new FrenziedRaptor(),        // Dinosaur
                new CoralMerfolk()           // Merfolk
        ));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // All four creature cards should be in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fathom Fleet Firebrand"))
                .anyMatch(c -> c.getName().equals("Barony Vampire"))
                .anyMatch(c -> c.getName().equals("Frenzied Raptor"))
                .anyMatch(c -> c.getName().equals("Coral Merfolk"));

        // Graveyard should only contain Grim Captain's Call itself
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName())
                .isEqualTo("Grim Captain's Call");
    }

    // ===== Resolution: partial matches =====

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Only Pirate and Dinosaur should be in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fathom Fleet Firebrand"))
                .anyMatch(c -> c.getName().equals("Frenzied Raptor"));
    }

    // ===== Resolution: no matches =====

    @Test
    @DisplayName("Does nothing when graveyard has no matching subtypes")
    void doesNothingWithNoMatchingSubtypes() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Grizzly Bears stays in graveyard, nothing returned to hand
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does nothing when graveyard is empty")
    void doesNothingWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Resolution: multiple cards of same subtype require choice =====

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Should be awaiting a graveyard choice for the Pirate
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Choose the first Pirate (index 0)
        harness.handleGraveyardCardChosen(player1, 0);

        // Chosen Pirate should be in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Other Pirate remains in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fathom Fleet Cutthroat")
                        || c.getName().equals("Fathom Fleet Firebrand"));
    }

    @Test
    @DisplayName("Choice for one subtype does not prevent auto-return of other subtypes")
    void choiceForOneSubtypeDoesNotBlockOthers() {
        harness.setGraveyard(player1, List.of(
                new FathomFleetFirebrand(),  // Pirate
                new FathomFleetCutthroat(),  // Pirate
                new BaronyVampire(),         // Vampire (single — auto-returned)
                new FrenziedRaptor()         // Dinosaur (single — auto-returned)
        ));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Vampire and Dinosaur auto-returned; Pirate requires choice
        // Vampire and Dinosaur should already be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Barony Vampire"))
                .anyMatch(c -> c.getName().equals("Frenzied Raptor"));

        // Should be awaiting graveyard choice for Pirate
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Choose Fathom Fleet Firebrand
        harness.handleGraveyardCardChosen(player1, 0);

        // Now 3 creature cards in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    // ===== Resolution: declining a choice does not skip other subtypes =====

    @Test
    @DisplayName("Declining a choice for one subtype still processes remaining subtypes")
    void decliningOneSubtypeProcessesRemaining() {
        harness.setGraveyard(player1, List.of(
                new FathomFleetFirebrand(),  // Pirate
                new FathomFleetCutthroat(),  // Pirate
                new FrenziedRaptor()         // Dinosaur (single — auto-returned after queue)
        ));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Dinosaur was auto-returned; Pirate requires choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Decline the Pirate choice
        harness.handleGraveyardCardChosen(player1, -1);

        // Dinosaur should still be in hand even though Pirate was declined
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Frenzied Raptor"));
        // Both Pirates should remain in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fathom Fleet Firebrand"))
                .anyMatch(c -> c.getName().equals("Fathom Fleet Cutthroat"));
    }

    // ===== Resolution: does not return cards from opponent's graveyard =====

    @Test
    @DisplayName("Only returns cards from controller's graveyard, not opponent's")
    void onlyReturnsFromControllersGraveyard() {
        harness.setGraveyard(player1, List.of(new FathomFleetFirebrand()));  // Pirate for player1
        harness.setGraveyard(player2, List.of(
                new BaronyVampire(),   // Vampire in opponent's graveyard
                new FrenziedRaptor()   // Dinosaur in opponent's graveyard
        ));
        harness.setHand(player1, List.of(new GrimCaptainsCall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Only Pirate from player1's graveyard should be returned
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName())
                .isEqualTo("Fathom Fleet Firebrand");

        // Opponent's graveyard should be unchanged
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
