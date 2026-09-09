package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodElves.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class WoodElvesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB presents only Forest cards, destined for the battlefield")
    void etbPresentsForestSearchToBattlefield() {
        setupAndCast();
        Forest forest = setupLibrary();

        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger → library search

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Forest enters the battlefield untapped")
    void chosenForestEntersBattlefieldUntapped() {
        setupAndCast();
        Forest forest = setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == forest
                        && !p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void mayFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No Forest in the library ends the search without prompting and shuffles")
    void noForestInLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Plains(), new Island(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gameLogContains("finds no Forest cards")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new WoodElves(), "{2}{G}");
    }

    private Forest setupLibrary() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new Plains(), forest, new Island(), new GrizzlyBears()));
        return forest;
    }
}
