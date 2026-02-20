package com.github.laxika.magicalvibes.cards.a;

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
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgonizingMemoriesTest {

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
    @DisplayName("Agonizing Memories has correct card properties")
    void hasCorrectProperties() {
        AgonizingMemories card = new AgonizingMemories();

        assertThat(card.getName()).isEqualTo("Agonizing Memories");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ChooseCardsFromTargetHandToTopOfLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Agonizing Memories");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving — choosing 2 cards from opponent's hand =====

    @Test
    @DisplayName("Resolving against hand with 2+ cards prompts for card choice")
    void promptsForCardChoice() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        Card card3 = new AgonizingMemories();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2, card3)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingRevealedHandChoiceRemainingCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing 2 cards puts them on top of target's library")
    void choosingTwoCardsFromHand() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        Card card3 = new AgonizingMemories();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2, card3)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Choose first card (index 0 = Grizzly Bears)
        harness.handleCardChosen(player1, 0);

        // Should still be awaiting another choice
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);
        assertThat(gd.interaction.awaitingRevealedHandChoiceRemainingCount).isEqualTo(1);

        // After removing Grizzly Bears, hand is [Peek, Agonizing Memories]
        // Choose index 0 = Peek
        harness.handleCardChosen(player1, 0);

        // Choice is complete
        assertThat(gd.interaction.awaitingInput).isNull();

        // The two chosen cards should be on top of player2's library
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck.get(0).getName()).isEqualTo("Grizzly Bears");
        assertThat(deck.get(1).getName()).isEqualTo("Peek");

        // Player2's hand should only have the remaining card
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Agonizing Memories");
    }

    @Test
    @DisplayName("Resolving against empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should not be awaiting any choice
        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("Resolving against hand with exactly 1 card chooses only that card")
    void singleCardHand() {
        Card onlyCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(onlyCard)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should prompt for 1 card (min of 2 and hand size 1)
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);
        assertThat(gd.interaction.awaitingRevealedHandChoiceRemainingCount).isEqualTo(1);

        // Choose the only card
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck.get(0).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Invalid card index is rejected")
    void invalidCardIndexRejected() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

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

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("Can target self")
    void canTargetSelf() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        Card card3 = new AgonizingMemories();
        harness.setHand(player1, new ArrayList<>(List.of(new AgonizingMemories(), card1, card2, card3)));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);

        // Choose cards from own hand
        harness.handleCardChosen(player1, 0);  // Grizzly Bears
        harness.handleCardChosen(player1, 0);  // Peek

        assertThat(gd.interaction.awaitingInput).isNull();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.get(0).getName()).isEqualTo("Grizzly Bears");
        assertThat(deck.get(1).getName()).isEqualTo("Peek");

        // Only Agonizing Memories should remain in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).get(0).getName()).isEqualTo("Agonizing Memories");
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Agonizing Memories"));
    }

    @Test
    @DisplayName("Hand reveal is logged")
    void handRevealIsLogged() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    @Test
    @DisplayName("Card choice is logged")
    void cardChoiceIsLogged() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("chooses") && log.contains("Grizzly Bears"));
    }
}

