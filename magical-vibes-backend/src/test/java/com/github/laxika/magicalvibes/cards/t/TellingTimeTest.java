package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TellingTimeTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Telling Time has correct card properties")
    void hasCorrectProperties() {
        TellingTime card = new TellingTime();

        assertThat(card.getName()).isEqualTo("Telling Time");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getCardText()).contains("Look at the top three cards");
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(LookAtTopCardsHandTopBottomEffect.class);
        LookAtTopCardsHandTopBottomEffect effect = (LookAtTopCardsHandTopBottomEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Telling Time puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Telling Time");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Resolving with 3+ cards =====

    @Test
    @DisplayName("Resolving enters hand/top/bottom choice state")
    void resolvingEntersHandTopBottomChoiceState() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.HAND_TOP_BOTTOM_CHOICE);
        assertThat(gd.interaction.awaitingHandTopBottomPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingHandTopBottomCards()).hasSize(3);
    }

    @Test
    @DisplayName("Telling Time goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Telling Time"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Choosing cards =====

    @Test
    @DisplayName("Choosing distributes cards to hand, top, and bottom correctly")
    void choosingDistributesCardsCorrectly() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);
        int originalDeckSize = deck.size();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Choose: card 1 to hand, card 0 to top, card 2 to bottom
        harness.getGameService().handleHandTopBottomChosen(gd, player1, 1, 0);

        // Hand should contain the chosen card
        assertThat(gd.playerHands.get(player1.getId())).contains(originalTop1);

        // Top of library should be the chosen top card
        assertThat(deck.get(0)).isSameAs(originalTop0);

        // Bottom of library should be the remaining card
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop2);

        // Deck size should be original minus 1 (one card went to hand, two back to deck)
        assertThat(deck).hasSize(originalDeckSize - 1);
    }

    @Test
    @DisplayName("Choosing first card for hand and last for top works correctly")
    void choosingFirstForHandLastForTop() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Choose: card 0 to hand, card 2 to top, card 1 to bottom
        harness.getGameService().handleHandTopBottomChosen(gd, player1, 0, 2);

        assertThat(gd.playerHands.get(player1.getId())).contains(originalTop0);
        assertThat(deck.get(0)).isSameAs(originalTop2);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop1);
    }

    @Test
    @DisplayName("Choosing clears awaiting state")
    void choosingClearsAwaitingState() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleHandTopBottomChosen(gd, player1, 0, 1);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.interaction.awaitingHandTopBottomPlayerId()).isNull();
        assertThat(gd.interaction.awaitingHandTopBottomCards()).isNull();
    }

    // ===== Library edge cases =====

    @Test
    @DisplayName("With 2 cards in library, one goes to hand and one to top")
    void twoCardsInLibrary() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        Card cardA = new GrizzlyBears();
        Card cardB = new GrizzlyBears();
        deck.add(cardA);
        deck.add(cardB);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.HAND_TOP_BOTTOM_CHOICE);
        assertThat(gd.interaction.awaitingHandTopBottomCards()).hasSize(2);

        // Choose cardA for hand, cardB for top
        harness.getGameService().handleHandTopBottomChosen(gd, player1, 0, 1);

        assertThat(gd.playerHands.get(player1.getId())).contains(cardA);
        assertThat(deck.get(0)).isSameAs(cardB);
        // No card should go to bottom — deck has exactly 1 card
        assertThat(deck).hasSize(1);
    }

    @Test
    @DisplayName("With 1 card in library, it automatically goes to hand")
    void oneCardInLibrary() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        Card singleCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(singleCard);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(singleCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top card"));
    }

    @Test
    @DisplayName("With empty library, nothing happens")
    void emptyLibrary() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Rejects same index for hand and top")
    void rejectsSameIndex() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() ->
                harness.getGameService().handleHandTopBottomChosen(gd, player1, 1, 1)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be different");
    }

    @Test
    @DisplayName("Rejects out-of-range hand card index")
    void rejectsOutOfRangeHandIndex() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() ->
                harness.getGameService().handleHandTopBottomChosen(gd, player1, 5, 0)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid hand card index");
    }

    @Test
    @DisplayName("Rejects out-of-range top card index")
    void rejectsOutOfRangeTopIndex() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() ->
                harness.getGameService().handleHandTopBottomChosen(gd, player1, 0, -1)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid top card index");
    }

    @Test
    @DisplayName("Rejects wrong player responding")
    void rejectsWrongPlayer() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() ->
                harness.getGameService().handleHandTopBottomChosen(gd, player2, 0, 1)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
    }

    @Test
    @DisplayName("Rejects response when not in hand/top/bottom choice state")
    void rejectsWhenNotAwaiting() {
        GameData gd = harness.getGameData();
        assertThatThrownBy(() ->
                harness.getGameService().handleHandTopBottomChosen(gd, player1, 0, 1)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not awaiting");
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log records looking at cards and distributing them")
    void gameLogRecordsActions() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top") && log.contains("3"));

        harness.getGameService().handleHandTopBottomChosen(gd, player1, 0, 1);
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("puts one card into their hand") && log.contains("on the bottom"));
    }

    @Test
    @DisplayName("Game log for 2-card case does not mention bottom")
    void gameLogTwoCardCase() {
        harness.setHand(player1, List.of(new TellingTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.getGameService().handleHandTopBottomChosen(gd, player1, 0, 1);
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("puts one card into their hand") && log.contains("on top of their library")
                        && !log.contains("bottom"));
    }
}


