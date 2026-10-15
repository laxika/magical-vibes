package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BrambleweftBehemoth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissasEncouragement.class, Forest.class, BrambleweftBehemoth.class,
        NissaGenesisMage.class, FeralProwler.class})
class NissasEncouragementTest extends BaseCardTest {

    @Test
    @DisplayName("Finds all three named cards from the library")
    void findsAllThreeFromLibrary() {
        castEncouragement();
        harness.setLibrary(player1, List.of(
                new Forest(),
                new BrambleweftBehemoth(),
                new NissaGenesisMage(),
                new FeralProwler()));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // Forest, then Brambleweft Behemoth, then Nissa, Genesis Mage.
        for (int i = 0; i < 3; i++) {
            var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search).isNotNull();
            assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
            assertThat(search.params().reveals()).isTrue();
            assertThat(search.params().canFailToFind()).isTrue();
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Forest", "Brambleweft Behemoth", "Nissa, Genesis Mage");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(e -> e.contains("shuffled"));
    }

    @Test
    @DisplayName("Takes a named card from the graveyard without prompting for it")
    void takesFromGraveyardThenLibrary() {
        harness.setGraveyard(player1, List.of(new Forest()));
        castEncouragement();
        harness.setLibrary(player1, List.of(new BrambleweftBehemoth(), new NissaGenesisMage(), new FeralProwler()));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // Forest already taken from GY; first library pick is Brambleweft Behemoth.
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().filterCardName()).isEqualTo("Brambleweft Behemoth");
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Forest", "Brambleweft Behemoth", "Nissa, Genesis Mage");
    }

    @Test
    @DisplayName("All three in graveyard go to hand and library still shuffles")
    void allFromGraveyardStillShuffles() {
        harness.setGraveyard(player1, List.of(
                new Forest(),
                new BrambleweftBehemoth(),
                new NissaGenesisMage()));
        castEncouragement();
        harness.setLibrary(player1, List.of(new FeralProwler()));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Brambleweft Behemoth", "Nissa, Genesis Mage");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Nissa's Encouragement");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(e -> e.contains("shuffled"));
    }

    @Test
    @DisplayName("May find none of the listed cards")
    void mayFindNone() {
        castEncouragement();
        harness.setLibrary(player1, List.of(new FeralProwler()));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(e -> e.contains("shuffled"));
    }

    @Test
    @DisplayName("May fail to find a library name and continue")
    void mayFailToFindOneName() {
        castEncouragement();
        harness.setLibrary(player1, List.of(
                new Forest(),
                new BrambleweftBehemoth(),
                new NissaGenesisMage()));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // Decline Forest, take the other two.
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .doesNotContain("Forest")
                .contains("Brambleweft Behemoth", "Nissa, Genesis Mage");
    }

    @Test
    @DisplayName("Can choose the library copy when the same name is in the graveyard")
    void canChooseLibraryCopyInsteadOfGraveyardCopy() {
        Forest graveyardForest = new Forest();
        Forest libraryForest = new Forest();
        harness.setGraveyard(player1, List.of(graveyardForest));
        castEncouragement();
        harness.setLibrary(player1, List.of(libraryForest));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(graveyardForest);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryForest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardForest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finds only one copy of each name and leaves opposing cards alone")
    void findsOnlyOneCopyAndSearchesOnlyOwnZones() {
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setGraveyard(player2, List.of(new BrambleweftBehemoth()));
        harness.setLibrary(player2, List.of(new NissaGenesisMage()));
        castEncouragement();
        harness.setLibrary(player1, List.of(firstForest, secondForest, new FeralProwler()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        GameData gd = harness.getGameData();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondForest);
        assertThat(gd.playerDecks.get(player1.getId())).contains(firstForest).doesNotContain(secondForest);
        harness.assertInGraveyard(player2, "Brambleweft Behemoth");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Nissa, Genesis Mage");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(e -> e.contains("shuffled"));
    }

    private void castEncouragement() {
        harness.setHand(player1, List.of(new NissasEncouragement()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
    }
}
