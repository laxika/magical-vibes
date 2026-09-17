package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurtleTracks.class, Forest.class, Island.class})
class TurtleTracksTest extends BaseCardTest {

    @Test
    @DisplayName("Each targeted player may search for an untapped basic land")
    void eachTargetedPlayerSearchesForUntappedBasicLand() {
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(island));
        castTurtleTracks(List.of(player1.getId(), player2.getId()));

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        harness.handleCardChosen(player1, 0);

        search = activeSearch();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("A targeted player may decline the search")
    void targetedPlayerMayDecline() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island()));
        castTurtleTracks(List.of(player2.getId()));

        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The spell may resolve with no targets")
    void noTargetsDoNothing() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island()));
        castTurtleTracks(List.of());

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void castTurtleTracks(List<UUID> targets) {
        harness.setHand(player1, List.of(new TurtleTracks()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, targets);
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
