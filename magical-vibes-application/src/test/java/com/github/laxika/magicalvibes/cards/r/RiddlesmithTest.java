package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Riddlesmith.class, Forest.class, GrizzlyBears.class, Spellbook.class, Memnite.class})
class RiddlesmithTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact cast puts the trigger on the stack before the draw choice")
    void artifactCastQueuesTriggerBeforeMayPrompt() {
        harness.addToBattlefield(player1, new Riddlesmith());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting draws a card then prompts for discard")
    void acceptDrawsThenPromptsDiscard() {
        harness.addToBattlefield(player1, new Riddlesmith());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        // Draw happened, now awaiting discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Full loot cycle: draw a card, discard a card, hand size stays same")
    void fullLootCycle() {
        harness.addToBattlefield(player1, new Riddlesmith());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new Spellbook(), bears));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Discard the bears at index 0
        harness.handleCardChosen(player1, 0);

        // Hand should have 1 card (the drawn Forest)
        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        // Graveyard should have the discarded card
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining may ability does not draw or discard")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new Riddlesmith());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.setLibrary(player1, List.of(new Forest()));

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // The resolved trigger is no longer on the stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Riddlesmith"));

        // Deck size unchanged (no draw happened)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Non-artifact spell does not trigger Riddlesmith")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Riddlesmith());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting artifact does not trigger Riddlesmith")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Riddlesmith());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("An artifact creature spell triggers Riddlesmith before entering the battlefield")
    void artifactCreatureTriggersBeforeEntering() {
        harness.addToBattlefield(player1, new Riddlesmith());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castCreature(player1, 0);

        harness.assertNotOnBattlefield(player1, "Memnite");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("The newly drawn card may be discarded instead of a card already in hand")
    void canDiscardNewlyDrawnCard() {
        harness.addToBattlefield(player1, new Riddlesmith());
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new Spellbook(), bears));
        harness.setLibrary(player1, List.of(forest));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears, forest);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Putting an artifact directly onto the battlefield does not trigger Riddlesmith")
    void artifactEnteringWithoutBeingCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new Riddlesmith());

        harness.addToBattlefield(player1, new Memnite());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
