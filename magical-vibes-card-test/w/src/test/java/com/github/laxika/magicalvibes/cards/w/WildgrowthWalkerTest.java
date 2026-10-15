package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BrazenBuccaneers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildgrowthWalker.class, BrazenBuccaneers.class, Forest.class})
class WildgrowthWalkerTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Explore with land on top puts a +1/+1 counter and gains 3 life")
    void exploreLandPutsCounterAndGainsLife() {
        Permanent walker = addWalkerReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();

        // Explore trigger resolves (no target needed)
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);
    }

    @Test
    @DisplayName("Explore with non-land (accept graveyard) puts a +1/+1 counter and gains 3 life")
    void exploreNonLandAcceptPutsCounterAndGainsLife() {
        Permanent walker = addWalkerReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new WildgrowthWalker());

        castExplorerAndResolveExplore();

        // May ability for explore graveyard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Resolve the trigger
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);
    }

    @Test
    @DisplayName("Explore with non-land (decline, keep on top) puts a +1/+1 counter and gains 3 life")
    void exploreNonLandDeclinePutsCounterAndGainsLife() {
        Permanent walker = addWalkerReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new WildgrowthWalker());

        castExplorerAndResolveExplore();

        // May ability for explore graveyard choice
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the trigger
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);
    }

    @Test
    @DisplayName("Multiple explores accumulate counters and life")
    void multipleExploresAccumulate() {
        Permanent walker = addWalkerReady(player1);

        // First explore (land)
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);

        // Second explore (land)
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 6);
    }

    @Test
    @DisplayName("Explore with empty library still puts a counter on Walker and gains 3 life")
    void exploreEmptyLibraryStillTriggers() {
        Permanent walker = addWalkerReady(player1);

        harness.setLibrary(player1, List.of());

        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);
    }

    @Test
    @DisplayName("If Walker is removed before trigger resolves, controller still gains life")
    void walkerRemovedBeforeTriggerResolvesStillGainsLife() {
        Permanent walker = addWalkerReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();

        // Remove walker from battlefield before trigger resolves
        gd.playerBattlefields.get(player1.getId()).remove(walker);

        harness.passBothPriorities();

        // Counter can't be placed (walker gone), but life gain still happens
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);
    }

    @Test
    @DisplayName("An opponent's creature exploring does not trigger Walker")
    void opponentExploreDoesNotTriggerWalker() {
        Permanent walker = addWalkerReady(player2);
        harness.setLibrary(player1, List.of(new Forest()));

        castExplorerAndResolveExplore();

        assertThat(gd.stack).isEmpty();
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, STARTING_LIFE);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("Each Walker triggers independently for one explore")
    void multipleWalkersEachTrigger() {
        Permanent first = addWalkerReady(player1);
        Permanent second = addWalkerReady(player1);
        harness.setLibrary(player1, List.of(new Forest()));

        castExplorerAndResolveExplore();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, STARTING_LIFE + 6);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("An explorer leaving before its explore resolves still triggers Walker")
    void explorerRemovedBeforeExploreStillTriggersWalker() {
        Permanent walker = addWalkerReady(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new BrazenBuccaneers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent explorer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BrazenBuccaneers)
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(explorer);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, STARTING_LIFE + 3);
    }

    private Permanent addWalkerReady(Player player) {
        return addCreatureReady(player, new WildgrowthWalker());
    }

    private void castExplorerAndResolveExplore() {
        harness.setHand(player1, List.of(new BrazenBuccaneers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB explore trigger
    }
}
