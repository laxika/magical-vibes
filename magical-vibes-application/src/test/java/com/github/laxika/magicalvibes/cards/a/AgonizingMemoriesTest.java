package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameLogSegment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Abeyance.class, AgonizingMemories.class, AlabasterDragon.class, GrizzlyBears.class, Peek.class})
class AgonizingMemoriesTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        Card spell = new AgonizingMemories();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getId()).isEqualTo(spell.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
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
        Card card1 = new Abeyance();
        Card card2 = new AlabasterDragon();
        Card card3 = new AgonizingMemories();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2, card3)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing 2 cards puts them on top of target's library")
    void choosingTwoCardsFromHand() {
        Card card1 = new Abeyance();
        Card card2 = new AlabasterDragon();
        Card card3 = new AgonizingMemories();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2, card3)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Choose the first card.
        harness.handleCardChosen(player1, 0);

        // Should still be awaiting another choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount()).isEqualTo(1);

        // After removing the first card, choose the next card at index 0.
        harness.handleCardChosen(player1, 0);

        // Choice is complete
        assertThat(gd.interaction.activeInteraction()).isNull();

        // The two chosen cards should be on top of player2's library
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck.get(0).getId()).isEqualTo(card1.getId());
        assertThat(deck.get(1).getId()).isEqualTo(card2.getId());

        // Player2's hand should only have the remaining card
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .containsExactly(card3.getId());
    }

    @Test
    @DisplayName("Places exactly two selected cards on top in selection order")
    void placesExactlyTwoCardsOnTopInSelectionOrder() {
        Card firstHandCard = new GrizzlyBears();
        Card secondHandCard = new Peek();
        Card existingLibraryTop = new AgonizingMemories();
        harness.setHand(player2, List.of(firstHandCard, secondHandCard));
        harness.setLibrary(player2, List.of(existingLibraryTop));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(secondHandCard, firstHandCard, existingLibraryTop);
    }

    @Test
    @DisplayName("Resolving against empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Should not be awaiting any choice
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("Resolving against hand with exactly 1 card chooses only that card")
    void singleCardHand() {
        Card onlyCard = new Abeyance();
        harness.setHand(player2, new ArrayList<>(List.of(onlyCard)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Should prompt for 1 card (min of 2 and hand size 1)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount()).isEqualTo(1);

        // Choose the only card
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck.get(0).getId()).isEqualTo(onlyCard.getId());
    }

    @Test
    @DisplayName("Invalid card index is rejected")
    void invalidCardIndexRejected() {
        Card card1 = new Abeyance();
        Card card2 = new AlabasterDragon();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Wrong player cannot choose")
    void wrongPlayerCannotChoose() {
        Card card1 = new Abeyance();
        Card card2 = new AlabasterDragon();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("Can target self")
    void canTargetSelf() {
        Card card1 = new Abeyance();
        Card card2 = new AlabasterDragon();
        Card card3 = new AgonizingMemories();
        Card spell = new AgonizingMemories();
        harness.setHand(player1, new ArrayList<>(List.of(spell, card1, card2, card3)));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);

        // Choose cards from own hand.
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.get(0).getId()).isEqualTo(card1.getId());
        assertThat(deck.get(1).getId()).isEqualTo(card2.getId());

        // Only the uncast Agonizing Memories should remain in hand.
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(card3.getId());
    }

    @Test
    @DisplayName("Can target only a player")
    void targetMustBePlayer() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new AlabasterDragon());
        Card spell = new AgonizingMemories();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(spell.getId());
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card card1 = new Abeyance();
        Card card2 = new AlabasterDragon();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        Card spell = new AgonizingMemories();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(spell.getId());
    }

    @Test
    @DisplayName("Looking at the hand is logged")
    void handLookIsLogged() {
        Card card1 = new Abeyance();
        Card card2 = new AlabasterDragon();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    @Test
    @DisplayName("Looking at the hand is visible only to the caster")
    void handLookIsPrivate() {
        Card card1 = new Abeyance();
        Card card2 = new AlabasterDragon();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anySatisfy(message -> assertThat(message)
                        .contains(card1.getId().toString(), card2.getId().toString()));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
    }

    @Test
    @DisplayName("Card choice is logged")
    void cardChoiceIsLogged() {
        Card card1 = new Abeyance();
        Card card2 = new AlabasterDragon();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new AgonizingMemories()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anySatisfy(log -> {
            assertThat(log.plainText()).contains("chooses");
            assertThat(log.segments()).anyMatch(segment -> segment instanceof GameLogSegment.CardSegment cardSegment
                    && cardSegment.card().getId().equals(card1.getId()));
        });
    }
}

