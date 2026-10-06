package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScoutingHawk.class, Forest.class, Plains.class, GrizzlyBears.class})
class ScoutingHawkTest extends BaseCardTest {

    @Test
    void entersAndSearchesForBasicPlainsOntoBattlefieldTapped() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new GrizzlyBears()));
        harness.addToBattlefield(player2, new Forest());

        castHawk();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1).allMatch(card -> card instanceof Plains);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Plains)
                .singleElement()
                .satisfies(land -> assertThat(land.isTapped()).isTrue());
    }

    @Test
    void doesNotSearchWhenNoOpponentControlsMoreLands() {
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castHawk();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card instanceof Plains);
    }

    @Test
    void doesNotSearchIfLandCountsBecomeEqualBeforeTriggerResolves() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.addToBattlefield(player2, new Forest());

        castHawk();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerRetroactivelyWhenOpponentGetsMoreLands() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));

        castHawk();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.addToBattlefield(player2, new Forest());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
    }

    @Test
    void canFailToFindEvenWhenBasicPlainsIsAvailable() {
        Plains plains = new Plains();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(plains, forest));
        harness.addToBattlefield(player2, new Forest());

        castHawk();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Plains);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void completesSearchWhenLibraryHasNoBasicPlains() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addToBattlefield(player2, new Forest());

        castHawk();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Plains);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void completesSearchWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player2, new Forest());

        castHawk();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castHawk() {
        harness.castFromHand(player1, new ScoutingHawk(), "{2}{W}");
    }
}
