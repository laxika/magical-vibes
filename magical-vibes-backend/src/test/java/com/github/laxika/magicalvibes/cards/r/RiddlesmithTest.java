package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardOnOwnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiddlesmithTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Riddlesmith has MayEffect wrapping DrawAndDiscardOnOwnSpellCastEffect with artifact filter")
    void hasCorrectStructure() {
        Riddlesmith card = new Riddlesmith();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(DrawAndDiscardOnOwnSpellCastEffect.class);
        DrawAndDiscardOnOwnSpellCastEffect trigger = (DrawAndDiscardOnOwnSpellCastEffect) mayEffect.wrapped();
        assertThat(trigger.spellFilter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) trigger.spellFilter()).cardType()).isEqualTo(CardType.ARTIFACT);
    }

    // ===== Trigger fires on artifact cast =====

    @Test
    @DisplayName("Casting an artifact spell triggers may ability prompt")
    void artifactCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new Riddlesmith());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Accept: draws a card, then prompts discard =====

    @Test
    @DisplayName("Accepting draws a card then prompts for discard")
    void acceptDrawsThenPromptsDiscard() {
        harness.addToBattlefield(player1, new Riddlesmith());
        harness.setHand(player1, List.of(new Spellbook()));
        setDeck(player1, List.of(new Forest()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Riddlesmith"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Draw happened, now awaiting discard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Full loot cycle: draw a card, discard a card, hand size stays same")
    void fullLootCycle() {
        harness.addToBattlefield(player1, new Riddlesmith());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new Spellbook(), bears));
        setDeck(player1, List.of(new Forest()));

        // Cast artifact (Spellbook at index 0) — hand becomes [GrizzlyBears]
        harness.castArtifact(player1, 0);

        // Accept loot
        harness.handleMayAbilityChosen(player1, true);

        // Resolve triggered ability — draws Forest, hand becomes [GrizzlyBears, Forest]
        harness.passBothPriorities();

        // Discard the bears at index 0
        harness.handleCardChosen(player1, 0);

        // Hand should have 1 card (the drawn Forest)
        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        // Graveyard should have the discarded card
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Decline =====

    @Test
    @DisplayName("Declining may ability does not draw or discard")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new Riddlesmith());
        harness.setHand(player1, List.of(new Spellbook()));
        setDeck(player1, List.of(new Forest()));

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Riddlesmith"));

        // Deck size unchanged (no draw happened)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    // ===== Non-artifact does not trigger =====

    @Test
    @DisplayName("Non-artifact spell does not trigger Riddlesmith")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Riddlesmith());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's artifact does not trigger =====

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
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== Helpers =====

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
