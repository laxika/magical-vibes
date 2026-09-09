package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StripMine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, Island.class, Mountain.class, Plains.class, StripMine.class, UntamedWilds.class})
class UntamedWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Presents basic lands with destination battlefield (untapped)")
    void resolvingPresentsBasicLandsToBattlefield() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(4)
                .allMatch(c -> c instanceof Plains
                        || c instanceof Forest
                        || c instanceof Island
                        || c instanceof Mountain)
                .noneMatch(StripMine.class::isInstance);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen basic land enters the battlefield untapped")
    void chosenBasicLandEntersUntapped() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Plains && !p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player can fail to find")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolving with no basic lands in library does not prompt")
    void noBasicLandsNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new UntamedWilds(), new UntamedWilds()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Searches only the controller's library")
    void onlySearchesControllersLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new UntamedWilds()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).anyMatch(Forest.class::isInstance);
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new UntamedWilds(), "{2}{G}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new Plains(), new Forest(), new Island(), new Mountain(), new StripMine()));
    }

    @Test
    @DisplayName("Chosen basic land is removed from the library and the library is shuffled")
    void chosenBasicLandIsRemovedAndLibraryIsShuffled() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        Card chosenCard = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(libraryBefore - 1)
                .noneMatch(card -> card.getId().equals(chosenCard.getId()));
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Resolving with an empty library does not prompt and still shuffles")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("it is empty")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }
}
