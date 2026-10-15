package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DireFleetCaptain;
import com.github.laxika.magicalvibes.cards.h.HeadstrongBrute;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfTheDrowned.class, JungleDelver.class, HeadstrongBrute.class, DireFleetCaptain.class})
class MarchOfTheDrownedTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 — prompts for creature target in graveyard")
    void mode0PromptsForCreatureTarget() {
        Card creature = new JungleDelver();
        Card march = new MarchOfTheDrowned();
        harness.setGraveyard(player1, List.of(creature, march));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).contains(creature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode 0 — selecting creature returns it to hand")
    void mode0ReturnsCreatureToHand() {
        Card creature = new JungleDelver();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jungle Delver");
        harness.assertNotInGraveyard(player1, "Jungle Delver");
    }

    @Test
    @DisplayName("Mode 0 — cannot cast without a creature in your graveyard")
    void mode0CannotCastWithoutCreature() {
        harness.setGraveyard(player1, List.of(new MarchOfTheDrowned()));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mode 1 — prompts for Pirate targets in graveyard")
    void mode1PromptsForPirateTargets() {
        Card pirate1 = new HeadstrongBrute();
        Card pirate2 = new DireFleetCaptain();
        Card nonPirate = new JungleDelver();
        harness.setGraveyard(player1, List.of(pirate1, pirate2, nonPirate));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .contains(pirate1.getId(), pirate2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 1 — selecting two Pirates returns them to hand")
    void mode1ReturnsTwoPiratesToHand() {
        Card pirate1 = new HeadstrongBrute();
        Card pirate2 = new DireFleetCaptain();
        harness.setGraveyard(player1, List.of(pirate1, pirate2));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(pirate1.getId(), pirate2.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Headstrong Brute");
        harness.assertInHand(player1, "Dire Fleet Captain");
        harness.assertNotInGraveyard(player1, "Headstrong Brute");
        harness.assertNotInGraveyard(player1, "Dire Fleet Captain");
    }

    @Test
    @DisplayName("Mode 1 — non-Pirate creatures are excluded from targets")
    void mode1ExcludesNonPirateCreatures() {
        Card pirate = new HeadstrongBrute();
        Card pirate2 = new DireFleetCaptain();
        Card nonPirate = new JungleDelver();
        harness.setGraveyard(player1, List.of(pirate, pirate2, nonPirate));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).containsExactlyInAnyOrder(pirate.getId(), pirate2.getId());
    }

    @Test
    @DisplayName("Mode 1 — cannot cast without two Pirates in your graveyard")
    void mode1CannotCastWithoutPirates() {
        harness.setGraveyard(player1, List.of(new JungleDelver()));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("March of the Drowned goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Card creature = new JungleDelver();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "March of the Drowned");
    }

    @Test
    void mode0RequiresOneTarget() {
        Card creature = new JungleDelver();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode1RequiresTwoTargets() {
        Card pirate1 = new HeadstrongBrute();
        Card pirate2 = new DireFleetCaptain();
        harness.setGraveyard(player1, List.of(pirate1, pirate2));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, 1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(pirate1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode1CannotCastWithOnlyOnePirate() {
        harness.setGraveyard(player1, List.of(new HeadstrongBrute()));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mode1ReturnsRemainingLegalTarget() {
        Card pirate1 = new HeadstrongBrute();
        Card pirate2 = new DireFleetCaptain();
        harness.setGraveyard(player1, List.of(pirate1, pirate2));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(pirate1.getId(), pirate2.getId()));
        harness.setGraveyard(player1, List.of(pirate2));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dire Fleet Captain");
        harness.assertNotInHand(player1, "Headstrong Brute");
        harness.assertNotInGraveyard(player1, "Dire Fleet Captain");
    }

    @Test
    void mode0OnlyOffersYourGraveyard() {
        Card ownCreature = new JungleDelver();
        Card opponentCreature = new HeadstrongBrute();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCreature.getId());
    }

    @Test
    void mode1DoesNotReturnTargetsThatLeftGraveyard() {
        Card pirate1 = new HeadstrongBrute();
        Card pirate2 = new DireFleetCaptain();
        harness.setGraveyard(player1, List.of(pirate1, pirate2));
        harness.setHand(player1, List.of(new MarchOfTheDrowned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(pirate1.getId(), pirate2.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Headstrong Brute");
        harness.assertNotInHand(player1, "Dire Fleet Captain");
        harness.assertInGraveyard(player1, "March of the Drowned");
    }
}
