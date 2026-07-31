package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BrambleweftBehemoth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NissasEncouragementTest extends BaseCardTest {

    @Test
    @DisplayName("Finds all three named cards from the library")
    void findsAllThreeFromLibrary() {
        castEncouragement();
        harness.setLibrary(player1, List.of(
                new Forest(),
                new BrambleweftBehemoth(),
                new NissaGenesisMage(),
                new GrizzlyBears()));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // Forest, then Brambleweft Behemoth, then Nissa, Genesis Mage.
        for (int i = 0; i < 3; i++) {
            var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search).isNotNull();
            assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
            assertThat(search.params().reveals()).isTrue();
            assertThat(search.params().canFailToFind()).isTrue();
            harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
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
        harness.setLibrary(player1, List.of(new BrambleweftBehemoth(), new NissaGenesisMage(), new GrizzlyBears()));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // Forest already taken from GY; first library pick is Brambleweft Behemoth.
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().filterCardName()).isEqualTo("Brambleweft Behemoth");
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

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
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .doesNotContain("Forest")
                .contains("Brambleweft Behemoth", "Nissa, Genesis Mage");
    }

    private void castEncouragement() {
        harness.setHand(player1, List.of(new NissasEncouragement()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
    }
}
