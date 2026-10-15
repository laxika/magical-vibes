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

    @Test
    @DisplayName("Bound equals lands controlled: with 2 lands, only cards with MV <= 2 are offered (any card type)")
    void boundEqualsLandsControlled() {
        castBeseech(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Library: Plains (MV 0), Goldenglow Moth (MV 1), Briarberry Cohort (MV 2), Kulrath Knight (MV 5).
        // With 2 lands, Plains, Goldenglow Moth, and Briarberry Cohort are eligible.
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

    @Test
    @DisplayName("When no card is within the bound, shuffles and logs no match")
    void noEligibleCardShufflesAndLogs() {
        castBeseech(0); // bound 0

        harness.setLibrary(player1, List.of(new GoldenglowMoth(), new BriarberryCohort()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no card with mana value"));
    }

    @Test
    @DisplayName("Land count is evaluated when the spell resolves")
    void usesLandCountAtResolution() {
        castBeseech(1);
        harness.setLibrary(player1, List.of(new BriarberryCohort(), new KulrathKnight()));
        harness.addToBattlefield(player1, new Plains());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting(Card::getName).containsExactly("Briarberry Cohort");
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Briarberry Cohort");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals Briarberry Cohort"));
    }

    @Test
    @DisplayName("Searching an empty library finishes without a choice")
    void emptyLibraryFinishesSearch() {
        castBeseech(2);
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three black mana can pay all three hybrid symbols")
    void canPayWithThreeBlackMana() {
        harness.setHand(player1, List.of(new BeseechTheQueen()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Plains");
    }

    @Test
    @DisplayName("Six colorless mana can pay all three hybrid symbols")
    void canPayWithSixColorlessMana() {
        harness.setHand(player1, List.of(new BeseechTheQueen()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castSorcery(player1, 0);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Plains");
    }

    private void castBeseech(int landsControlled) {
        for (int i = 0; i < landsControlled; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
        harness.setHand(player1, List.of(new BeseechTheQueen()));
        harness.addMana(player1, ManaColor.BLACK, 6); // {2/B}{2/B}{2/B}
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new GoldenglowMoth(),
                new BriarberryCohort(), new KulrathKnight()));
    }
}
