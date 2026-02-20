package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TraumatizeTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Traumatize has correct card properties")
    void hasCorrectProperties() {
        Traumatize card = new Traumatize();

        assertThat(card.getName()).isEqualTo("Traumatize");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{3}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(MillHalfLibraryEffect.class);
    }

    // ===== Milling =====

    @Test
    @DisplayName("Mills half of target player's library rounded down (even count)")
    void millsHalfLibraryEvenCount() {
        harness.setHand(player1, List.of(new Traumatize()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 20) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
    }

    @Test
    @DisplayName("Mills half of target player's library rounded down (odd count)")
    void millsHalfLibraryOddCount() {
        harness.setHand(player1, List.of(new Traumatize()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 11) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 11 / 2 = 5 (rounded down), so 6 remain
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Traumatize()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        // 5 milled cards + Traumatize itself goes to graveyard after resolving
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Traumatize"));
    }

    @Test
    @DisplayName("Mills nothing when library has only 1 card")
    void millsNothingWithOneCard() {
        harness.setHand(player1, List.of(new Traumatize()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 1) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 1 / 2 = 0, mills nothing
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mills nothing when library is empty")
    void millsNothingWithEmptyLibrary() {
        harness.setHand(player1, List.of(new Traumatize()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        gd.playerDecks.get(player2.getId()).clear();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Traumatize goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Traumatize()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Traumatize"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Milled cards move from top of library to graveyard in order")
    void milledCardsAreFromTopOfLibrary() {
        harness.setHand(player1, List.of(new Traumatize()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 4) {
            deck.removeFirst();
        }

        // Record the top 2 cards (half of 4)
        Card firstCard = deck.get(0);
        Card secondCard = deck.get(1);
        Card thirdCard = deck.get(2);
        Card fourthCard = deck.get(3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Top 2 should be milled, bottom 2 remain
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(thirdCard, fourthCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstCard, secondCard);
    }
}

