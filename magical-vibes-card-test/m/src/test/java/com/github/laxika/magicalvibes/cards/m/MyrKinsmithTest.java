package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DuneMover;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyrKinsmith.class, DuneMover.class})
class MyrKinsmithTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates an optional search for a Myr card")
    void etbCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the ETB search offers only Myr cards")
    void acceptingSearchOffersOnlyMyrCards() {
        setupAndCast();
        setupLibrary();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1);
        assertThat(offered).allMatch(card -> card.getSubtypes().contains(CardSubtype.MYR));
    }

    @Test
    @DisplayName("Choosing a Myr card puts it into hand")
    void choosingMyrPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB search leaves the library untouched")
    void decliningSearchSkipsLibrarySearch() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A chosen Myr is revealed and removed from the controller's library")
    void searchRevealsChosenMyrAndLeavesOtherLibrariesAlone() {
        setupAndCast();
        MyrKinsmith chosen = new MyrKinsmith();
        DuneMover other = new DuneMover();
        MyrKinsmith opponentsCard = new MyrKinsmith();
        harness.setLibrary(player1, List.of(chosen, other));
        harness.setLibrary(player2, List.of(opponentsCard));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals Myr Kinsmith"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find even when a Myr is available")
    void canFailToFindAvailableMyr() {
        setupAndCast();
        MyrKinsmith myr = new MyrKinsmith();
        DuneMover other = new DuneMover();
        harness.setLibrary(player1, List.of(myr, other));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(myr, other);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching a library with no Myr finishes without taking a card")
    void searchWithNoMatchingCardsCompletes() {
        setupAndCast();
        DuneMover other = new DuneMover();
        harness.setLibrary(player1, List.of(other));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library finishes normally")
    void searchEmptyLibraryCompletes() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining preserves library order and does not shuffle")
    void decliningPreservesLibraryAndHand() {
        setupAndCast();
        MyrKinsmith myr = new MyrKinsmith();
        DuneMover other = new DuneMover();
        harness.setLibrary(player1, List.of(myr, other));

        resolveEtb();
        int logsBefore = gd.gameLog.size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(myr, other);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.subList(logsBefore, gd.gameLog.size()))
                .noneMatch(entry -> entry.plainText().contains("shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new MyrKinsmith()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new MyrKinsmith(), new DuneMover()));
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
