package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuressTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Duress has correct card properties")
    void hasCorrectProperties() {
        Duress card = new Duress();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ChooseCardFromTargetHandToDiscardEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Duress");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Duress()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving — choosing a noncreature, nonland card from opponent's hand =====

    @Test
    @DisplayName("Resolving reveals hand and prompts caster for card choice")
    void promptsForCardChoice() {
        Card card1 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.revealedHandChoice().remainingCount()).isEqualTo(1);
        assertThat(gd.interaction.revealedHandChoice().discardMode()).isTrue();
    }

    @Test
    @DisplayName("Choosing a noncreature nonland card discards it")
    void choosingValidCardDiscardsIt() {
        Card instant = new Peek();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(instant, creature)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Choose Peek (index 0)
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();

        // Peek should be in player2's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));

        // Grizzly Bears should remain in player2's hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Creature cards are excluded from valid choices")
    void creatureCardsExcludedFromChoices() {
        Card creature = new GrizzlyBears();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(creature, instant)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);

        // Only index 1 (Peek) should be valid, index 0 (Grizzly Bears) is a creature
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Land cards are excluded from valid choices")
    void landCardsExcludedFromChoices() {
        Card land = new Forest();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(land, instant)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);

        // Only index 1 (Peek) should be valid, index 0 (Forest) is a land
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Selecting a creature index is rejected")
    void selectingCreatureIndexIsRejected() {
        Card creature = new GrizzlyBears();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(creature, instant)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Trying to choose Grizzly Bears (index 0, a creature) should fail
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Hand with only creatures and lands results in no valid choices")
    void handWithOnlyCreaturesAndLandsNoValidChoices() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(creature, land)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No valid choices, so the effect should complete without prompting
        assertThat(gd.interaction.awaitingInputType()).isNull();

        // Both cards should remain in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        // Log should indicate no valid choices
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Resolving against empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("Hand with mix of all types only allows noncreature nonland choice")
    void mixedHandOnlyAllowsValidChoice() {
        Card land = new Forest();
        Card instant = new Peek();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(land, instant, creature)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);
        // Only index 1 (Peek) should be valid
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(1);

        // Choose the only valid card
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));

        // Forest and Grizzly Bears remain in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Invalid card index is rejected")
    void invalidCardIndexRejected() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Wrong player cannot choose")
    void wrongPlayerCannotChoose() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Duress goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Duress"));
    }

    // ===== Logging =====

    @Test
    @DisplayName("Hand reveal is logged")
    void handRevealIsLogged() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    @DisplayName("Card choice is logged")
    void cardChoiceIsLogged() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("chooses") && log.contains("Peek"));
    }

    @Test
    @DisplayName("Discard is logged")
    void discardIsLogged() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("discards") && log.contains("Peek"));
    }
}
