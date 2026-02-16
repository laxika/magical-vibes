package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistressTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Distress has correct card properties")
    void hasCorrectProperties() {
        Distress card = new Distress();

        assertThat(card.getName()).isEqualTo("Distress");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ChooseCardFromTargetHandToDiscardEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Distress");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving — choosing a nonland card from opponent's hand =====

    @Test
    @DisplayName("Resolving reveals hand and prompts caster for card choice")
    void promptsForCardChoice() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);
        assertThat(gd.awaitingCardChoicePlayerId).isEqualTo(player1.getId());
        assertThat(gd.awaitingRevealedHandChoiceRemainingCount).isEqualTo(1);
        assertThat(gd.awaitingRevealedHandChoiceDiscardMode).isTrue();
    }

    @Test
    @DisplayName("Choosing a nonland card discards it to opponent's graveyard")
    void choosingNonlandCardDiscardsIt() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Choose Grizzly Bears (index 0)
        harness.handleCardChosen(player1, 0);

        // Choice is complete
        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.awaitingRevealedHandChoiceDiscardMode).isFalse();

        // Grizzly Bears should be in player2's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Peek should remain in player2's hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Peek");
    }

    @Test
    @DisplayName("Land cards are excluded from valid choices")
    void landCardsExcludedFromChoices() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(creature, land)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);

        // Only index 0 (Grizzly Bears) should be valid, index 1 (Forest) is a land
        assertThat(gd.awaitingCardChoiceValidIndices).containsExactly(0);
    }

    @Test
    @DisplayName("Selecting a land index is rejected")
    void selectingLandIndexIsRejected() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(creature, land)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Trying to choose the Forest (index 1, a land) should fail
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Hand with only lands results in no valid choices")
    void handWithOnlyLandsNoValidChoices() {
        Card land1 = new Forest();
        Card land2 = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(land1, land2)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No valid choices, so the effect should complete without prompting
        assertThat(gd.awaitingInput).isNull();

        // Lands should remain in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        // Log should indicate no valid choices
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Resolving against empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("Hand with mix of lands and nonlands only allows nonland choice")
    void mixedHandOnlyAllowsNonlandChoice() {
        Card land1 = new Forest();
        Card creature = new GrizzlyBears();
        Card land2 = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(land1, creature, land2)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);
        // Only index 1 (Grizzly Bears) should be valid
        assertThat(gd.awaitingCardChoiceValidIndices).containsExactly(1);

        // Choose the only nonland card
        harness.handleCardChosen(player1, 1);

        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Two forests remain in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId()))
                .allMatch(c -> c.getName().equals("Forest"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Invalid card index is rejected")
    void invalidCardIndexRejected() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Wrong player cannot choose")
    void wrongPlayerCannotChoose() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target self")
    void canTargetSelf() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player1, new ArrayList<>(List.of(new Distress(), card1, card2)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);

        // Choose Grizzly Bears (index 0 after Distress left hand)
        harness.handleCardChosen(player1, 0);

        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Peek should remain in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).get(0).getName()).isEqualTo("Peek");
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Distress goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card card1 = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(card1)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Distress"));
    }

    // ===== Logging =====

    @Test
    @DisplayName("Hand reveal is logged")
    void handRevealIsLogged() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    @DisplayName("Card choice is logged")
    void cardChoiceIsLogged() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("chooses") && log.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Discard is logged")
    void discardIsLogged() {
        Card card1 = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(card1)));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("discards") && log.contains("Grizzly Bears"));
    }
}
