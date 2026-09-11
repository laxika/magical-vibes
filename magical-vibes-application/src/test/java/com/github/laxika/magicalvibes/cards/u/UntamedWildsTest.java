package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DwarvenRuins;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UntamedWilds.class, Plains.class, Forest.class, Island.class, Mountain.class, DwarvenRuins.class})
class UntamedWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Presents basic lands with destination battlefield (untapped)")
    void resolvingPresentsBasicLandsToBattlefield() {
        castAndOpenLibrarySearch();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(4)
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen basic land enters the battlefield untapped")
    void chosenBasicLandEntersUntapped() {
        castAndOpenLibrarySearch();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && !p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player can fail to find")
    void canFailToFind() {
        castAndOpenLibrarySearch();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chosen basic land leaves the library and the library is shuffled")
    void chosenBasicLandLeavesLibraryAndShuffles() {
        castAndOpenLibrarySearch();

        Card chosenCard = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(chosenCard);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
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
    @DisplayName("Resolving with an empty library does not prompt and still shuffles")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("it is empty")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private void castAndOpenLibrarySearch() {
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new UntamedWilds(), "{2}{G}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new Plains(), new Forest(), new Island(), new Mountain(), new DwarvenRuins()));
    }
}
