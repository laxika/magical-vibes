package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BrazenBuccaneers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WildgrowthWalkerTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    // ===== Card effect configuration =====

    @Test
    @DisplayName("Has ON_ALLY_CREATURE_EXPLORES effects: PutCounterOnSelfEffect and GainLifeEffect")
    void hasExploreTriggeredEffects() {
        WildgrowthWalker card = new WildgrowthWalker();

        var effects = card.getEffects(EffectSlot.ON_ALLY_CREATURE_EXPLORES);
        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(PutCounterOnSelfEffect.class);
        PutCounterOnSelfEffect counterEffect = (PutCounterOnSelfEffect) effects.get(0);
        assertThat(counterEffect.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
        assertThat(effects.get(1)).isInstanceOf(GainLifeEffect.class);
        GainLifeEffect lifeEffect = (GainLifeEffect) effects.get(1);
        assertThat(lifeEffect.amount()).isEqualTo(3);
    }

    // ===== Explore triggers — land on top =====

    @Test
    @DisplayName("Explore with land on top puts a +1/+1 counter and gains 3 life")
    void exploreLandPutsCounterAndGainsLife() {
        Permanent walker = addWalkerReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();

        // Explore trigger resolves (no target needed)
        harness.passBothPriorities();

        assertThat(walker.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);
    }

    // ===== Explore triggers — non-land on top =====

    @Test
    @DisplayName("Explore with non-land (accept graveyard) puts a +1/+1 counter and gains 3 life")
    void exploreNonLandAcceptPutsCounterAndGainsLife() {
        Permanent walker = addWalkerReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        castExplorerAndResolveExplore();

        // May ability for explore graveyard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);

        // Resolve the trigger
        harness.passBothPriorities();

        assertThat(walker.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);
    }

    @Test
    @DisplayName("Explore with non-land (decline, keep on top) puts a +1/+1 counter and gains 3 life")
    void exploreNonLandDeclinePutsCounterAndGainsLife() {
        Permanent walker = addWalkerReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        castExplorerAndResolveExplore();

        // May ability for explore graveyard choice
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the trigger
        harness.passBothPriorities();

        assertThat(walker.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);
    }

    // ===== Multiple explores =====

    @Test
    @DisplayName("Multiple explores accumulate counters and life")
    void multipleExploresAccumulate() {
        Permanent walker = addWalkerReady(player1);

        // First explore (land)
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        assertThat(walker.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);

        // Second explore (land)
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        assertThat(walker.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 6);
    }

    // ===== Explore with empty library =====

    @Test
    @DisplayName("Explore with empty library does not trigger")
    void exploreEmptyLibraryNoTrigger() {
        Permanent walker = addWalkerReady(player1);

        gd.playerDecks.get(player1.getId()).clear();

        castExplorerAndResolveExplore();

        assertThat(walker.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE);
    }

    // ===== Walker removed before trigger resolves =====

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

    // ===== Helpers =====

    private Permanent addWalkerReady(Player player) {
        Permanent perm = new Permanent(new WildgrowthWalker());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
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
