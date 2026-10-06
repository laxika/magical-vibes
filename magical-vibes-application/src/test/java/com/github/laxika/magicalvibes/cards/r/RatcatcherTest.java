package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.b.BronzeBombshell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ratcatcher.class, BogRats.class, GrizzlyBears.class, BronzeBombshell.class})
class RatcatcherTest extends BaseCardTest {
    @Test
    @DisplayName("A colorless artifact creature can block Ratcatcher")
    void canBeBlockedByArtifactCreature() {
        addCreatureReady(player1, new Ratcatcher());
        var blocker = addCreatureReady(player2, new BronzeBombshell());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A found Rat is revealed and only one card is put into hand")
    void revealsOneRatAndShuffles() {
        harness.addToBattlefield(player1, new Ratcatcher());
        harness.setHand(player1, List.of());
        BogRats firstRat = new BogRats();
        BogRats secondRat = new BogRats();
        harness.setLibrary(player1, List.of(firstRat, secondRat));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstRat);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondRat);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("An accepted search can fail to find even when a Rat is present")
    void canFailToFindAnAvailableRat() {
        harness.addToBattlefield(player1, new Ratcatcher());
        harness.setHand(player1, List.of());
        BogRats rat = new BogRats();
        harness.setLibrary(player1, List.of(rat));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(rat);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("An accepted search of an empty library finishes and shuffles")
    void searchesAnEmptyLibrary() {
        harness.addToBattlefield(player1, new Ratcatcher());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Fear prevents a green nonartifact creature from blocking Ratcatcher")
    void cannotBeBlockedByGreenNonartifactCreature() {
        addCreatureReady(player1, new Ratcatcher());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("A black creature can block Ratcatcher")
    void canBeBlockedByBlackCreature() {
        addCreatureReady(player1, new Ratcatcher());
        var blocker = addCreatureReady(player2, new BogRats());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Upkeep trigger may search the library for a Rat")
    void searchesForRat() {
        harness.addToBattlefield(player1, new Ratcatcher());
        harness.setHand(player1, List.of());
        BogRats rat = new BogRats();
        GrizzlyBears nonRat = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonRat, rat));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(rat);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(rat);
        assertThat(gd.playerDecks.get(player1.getId())).contains(nonRat).doesNotContain(rat);
    }

    @Test
    @DisplayName("Declining the upkeep trigger does not search")
    void declinesSearch() {
        harness.addToBattlefield(player1, new Ratcatcher());
        BogRats rat = new BogRats();
        harness.setLibrary(player1, List.of(rat));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(rat);
        assertThat(gd.playerDecks.get(player1.getId())).contains(rat);
    }

    @Test
    @DisplayName("Searching may fail to find a Rat")
    void findsNoRatWhenLibraryHasNoRat() {
        harness.addToBattlefield(player1, new Ratcatcher());
        GrizzlyBears nonRat = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonRat));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonRat);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonRat);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Ratcatcher());
        BogRats rat = new BogRats();
        harness.setLibrary(player1, List.of(rat));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(rat);
    }
}
