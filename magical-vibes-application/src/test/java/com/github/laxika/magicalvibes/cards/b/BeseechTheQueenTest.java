package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GoldenglowMoth;
import com.github.laxika.magicalvibes.cards.k.KulrathKnight;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeseechTheQueen.class, Plains.class, GoldenglowMoth.class,
        BriarberryCohort.class, KulrathKnight.class})
class BeseechTheQueenTest extends BaseCardTest {

    // ===== Mana-value bound driven by lands controlled =====

    @Test
    @DisplayName("Bound equals lands controlled: with 2 lands, only cards with MV <= 2 are offered (any card type)")
    void boundEqualsLandsControlled() {
        castBeseech(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Library: Plains (MV 0), Goldenglow Moth (MV 1), Briarberry Cohort (MV 2), Kulrath Knight (MV 5).
        // 2 lands controlled → MV <= 2: Plains, Goldenglow Moth, Briarberry Cohort. The land IS eligible
        // (null filter = any card), unlike Citanul Flute / Green Sun's Zenith which filter by type.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Goldenglow Moth", "Briarberry Cohort");
    }

    @Test
    @DisplayName("More lands raises the bound: 5 lands makes every card in the library eligible")
    void moreLandsRaisesBound() {
        castBeseech(5);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Goldenglow Moth", "Briarberry Cohort", "Kulrath Knight");
    }

    @Test
    @DisplayName("Zero lands means only mana value 0 cards qualify")
    void zeroLandsBoundsToManaValueZero() {
        castBeseech(0);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName())
                .isEqualTo("Plains");
    }

    @Test
    @DisplayName("Only the caster's lands count toward the bound")
    void countsOnlyLandsControlledByCaster() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Plains());
        }
        harness.addToBattlefield(player1, new BriarberryCohort());
        castBeseech(0);
        harness.setLibrary(player1, List.of(new GoldenglowMoth()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("finds no card with mana value"));
    }

    // ===== Choosing / revealing =====

    @Test
    @DisplayName("Chosen card is revealed, put into hand, and library shuffled")
    void chosenCardGoesToHand() {
        castBeseech(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.HAND);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Fail to find =====

    @Test
    @DisplayName("Search can fail to find; choosing index -1 takes nothing and just shuffles")
    void canFailToFind() {
        castBeseech(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== No match =====

    @Test
    @DisplayName("When no card is within the bound, shuffles and logs no match")
    void noEligibleCardShufflesAndLogs() {
        castBeseech(0); // bound 0

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GoldenglowMoth(), new BriarberryCohort())); // lowest MV is 1

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no card with mana value"));
    }

    // ===== Helpers =====

    private void castBeseech(int landsControlled) {
        for (int i = 0; i < landsControlled; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
        harness.setHand(player1, List.of(new BeseechTheQueen()));
        harness.addMana(player1, ManaColor.BLACK, 6); // {2/B}{2/B}{2/B}
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        // Plains: MV 0 (land), Goldenglow Moth: MV 1, Briarberry Cohort: MV 2, Kulrath Knight: MV 5
        deck.addAll(List.of(new Plains(), new GoldenglowMoth(), new BriarberryCohort(), new KulrathKnight()));
    }
}
