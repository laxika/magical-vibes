package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IllusionaryTerrain;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PaleBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkeletonShip.class, Island.class, PaleBears.class, IllusionaryTerrain.class, Forest.class})
class SkeletonShipTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenControllingNoIslands() {
        harness.castFromHand(player1, new SkeletonShip(), "{3}{U}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Skeleton Ship");
        harness.assertInGraveyard(player1, "Skeleton Ship");
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWhileControllingIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new SkeletonShip(), "{3}{U}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Skeleton Ship");
    }

    @Test
    @DisplayName("{T}: puts a -1/-1 counter on target creature")
    void tapPutsMinusOneCounter() {
        addReadySkeletonShip(player1);
        Permanent bears = addCreatureReady(player2, new PaleBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two activations kill a 2/2 creature")
    void twoActivationsKill2Toughness() {
        Permanent ship = addReadySkeletonShip(player1);
        Permanent bears = addCreatureReady(player2, new PaleBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        ship.untap();
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pale Bears");
        harness.assertInGraveyard(player2, "Pale Bears");
    }

    @Test
    @DisplayName("{T}: cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent ship = addReadySkeletonShip(player1);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(ship.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Survives when a controlled land has become an Island")
    void survivesWhenControlledLandHasBecomeIsland() {
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());
        terrain.setChosenSubtype(CardSubtype.FOREST);
        terrain.setSecondChosenSubtype(CardSubtype.ISLAND);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);

        addCreatureReady(player1, new SkeletonShip());
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Skeleton Ship");
    }

    @Test
    @DisplayName("An opponent's Island does not prevent the sacrifice trigger")
    void opponentsIslandDoesNotPreventSacrifice() {
        harness.addToBattlefield(player2, new Island());
        harness.castFromHand(player1, new SkeletonShip(), "{3}{U}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skeleton Ship");
        harness.assertInGraveyard(player1, "Skeleton Ship");
    }

    @Test
    @DisplayName("Gaining an Island after the trigger fires does not prevent sacrifice")
    void gainingIslandDoesNotStopPendingSacrifice() {
        harness.castFromHand(player1, new SkeletonShip(), "{3}{U}{B}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Island());
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skeleton Ship");
        harness.assertInGraveyard(player1, "Skeleton Ship");
    }

    @Test
    @DisplayName("Losing the Island subtype triggers once while the ability is pending")
    void losingIslandSubtypeTriggersOnlyOnce() {
        addReadySkeletonShip(player1);
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());
        terrain.setChosenSubtype(CardSubtype.ISLAND);
        terrain.setSecondChosenSubtype(CardSubtype.FOREST);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Skeleton Ship");
        harness.assertInGraveyard(player1, "Skeleton Ship");
    }

    @Test
    @DisplayName("Can target itself and pays the tap cost immediately")
    void canTargetItself() {
        Permanent ship = addReadySkeletonShip(player1);

        harness.activateAbility(player1, 0, null, ship.getId());

        assertThat(ship.isTapped()).isTrue();
        assertThat(ship.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(ship.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Skeleton Ship");
    }

    private Permanent addReadySkeletonShip(Player player) {
        Permanent perm = addCreatureReady(player, new SkeletonShip());
        harness.addToBattlefield(player, new Island()); // keep Skeleton Ship from being sacrificed
        return perm;
    }
}
