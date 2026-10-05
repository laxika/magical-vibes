package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.n.NotionThief;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltenPsyche.class, GrizzlyBears.class, LeoninScimitar.class, Spellbook.class,
        AlmsCollector.class, NotionThief.class})
class MoltenPsycheTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws the same number of cards as they had in hand")
    void wheelPreservesHandSize() {
        // Player 1 has 3 cards in hand (Molten Psyche + 2 others)
        // Player 2 has 2 cards in hand
        Card filler1 = new GrizzlyBears();
        Card filler2 = new GrizzlyBears();
        Card filler3 = new GrizzlyBears();
        Card filler4 = new GrizzlyBears();

        harness.setHand(player1, List.of(new MoltenPsyche(), filler1, filler2));
        harness.setHand(player2, List.of(filler3, filler4));

        // Ensure enough cards in libraries to draw
        fillDeck(player1, 10);
        fillDeck(player2, 10);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Cast Molten Psyche — after casting, player1's hand has 2 remaining cards
        harness.castAndResolveSorcery(player1, 0, 0);

        // Player1 had 2 cards remaining when spell resolved (3 minus the cast Molten Psyche),
        // so draws 2. Player2 had 2 cards, so draws 2.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Player with empty hand draws zero cards")
    void emptyHandDrawsZero() {
        harness.setHand(player1, List.of(new MoltenPsyche()));
        harness.setHand(player2, List.of());

        fillDeck(player1, 10);
        fillDeck(player2, 10);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player1 had 0 cards in hand after casting (only had Molten Psyche), so draws 0
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Player2 had 0 cards in hand, so draws 0
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Original hand cards are shuffled into library, not kept")
    void handCardsGoIntoLibrary() {
        Card uniqueCard = new GrizzlyBears();
        harness.setHand(player2, List.of(uniqueCard));

        harness.setHand(player1, List.of(new MoltenPsyche()));
        fillDeck(player1, 10);
        fillDeck(player2, 10);

        int libraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player2 drew 1 new card from library; the unique card was shuffled into library
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        // Library size unchanged (1 shuffled in from hand, 1 drawn out), proving
        // the hand card was shuffled into the library rather than discarded
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore);
    }

    @Test
    @DisplayName("No damage dealt without metalcraft")
    void noDamageWithoutMetalcraft() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MoltenPsyche()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        fillDeck(player1, 10);
        fillDeck(player2, 10);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Opponent should take no damage without metalcraft
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With metalcraft, deals damage equal to cards drawn this turn")
    void dealsDamageEqualToCardsDrawnWithMetalcraft() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MoltenPsyche()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        fillDeck(player1, 10);
        fillDeck(player2, 10);

        addThreeArtifacts(player1);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player2 had 3 cards in hand → shuffled into library → drew 3
        // Metalcraft damage = 3 cards drawn this turn
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("With metalcraft, counts cards drawn from draw step too")
    void metalcraftCountsDrawStepCards() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MoltenPsyche()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        fillDeck(player1, 10);
        fillDeck(player2, 10);

        addThreeArtifacts(player1);

        // Simulate player2 having drawn 1 card already this turn (e.g. from draw step)
        gd.cardsDrawnThisTurn.put(player2.getId(), 1);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player2 had 2 cards → drew 2 from wheel + 1 prior = 3 total drawn this turn
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("With metalcraft, no damage if opponent drew 0 cards (empty hand, no prior draws)")
    void metalcraftNoDamageIfZeroCardsDrawn() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MoltenPsyche()));
        harness.setHand(player2, List.of());

        fillDeck(player1, 10);
        fillDeck(player2, 10);

        addThreeArtifacts(player1);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player2 had empty hand → drew 0, no prior draws → 0 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No damage if metalcraft lost before resolution")
    void noDamageIfMetalcraftLostBeforeResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MoltenPsyche()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        fillDeck(player1, 10);
        fillDeck(player2, 10);

        addThreeArtifacts(player1);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        // Remove artifacts before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Wheel still happens, but no damage without metalcraft
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Alms Collector replaces the opponent's entire multi-card draw before metalcraft damage")
    void multiCardDrawIsReplacedBeforeDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MoltenPsyche()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        fillDeck(player1, 10);
        fillDeck(player2, 10);
        addThreeArtifacts(player1);
        harness.addToBattlefield(player1, new AlmsCollector());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Every hand is shuffled before Notion Thief redirects any draws")
    void allHandsAreShuffledBeforeDrawing() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MoltenPsyche(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        fillDeck(player1, 10);
        fillDeck(player2, 10);
        addThreeArtifacts(player1);
        harness.addToBattlefield(player2, new NotionThief());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.cardsDrawnThisTurn.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Two artifacts are insufficient even if the opponent controls three")
    void onlyControllerArtifactsCountTowardMetalcraft() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MoltenPsyche()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        fillDeck(player1, 10);
        fillDeck(player2, 10);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        addThreeArtifacts(player2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Metalcraft gained after casting applies at resolution")
    void metalcraftGainedBeforeResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MoltenPsyche(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        fillDeck(player1, 10);
        fillDeck(player2, 10);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        addThreeArtifacts(player1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Shuffled hand cards can supply the draws when libraries were empty")
    void emptyLibrariesCanDrawShuffledHandCards() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new MoltenPsyche(), firstCard));
        harness.setHand(player2, List.of(secondCard));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private void addThreeArtifacts(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new Spellbook());
        harness.addToBattlefield(player, new LeoninScimitar());
        harness.addToBattlefield(player, new Spellbook());
    }

    private void fillDeck(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Card> deck = new ArrayList<>(gd.playerDecks.getOrDefault(player.getId(), List.of()));
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
        harness.setLibrary(player, deck);
    }
}
