package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParkHeightsMaverick.class, GrizzlyBears.class, HillGiant.class})
class ParkHeightsMaverickTest extends BaseCardTest {

    @Test
    @DisplayName("Dethrone puts a +1/+1 counter on it when attacking the player with most life")
    void dethronePutsCounterWhenAttackingPlayerWithMostLife() {
        Permanent maverick = addCreatureReady(player1, new ParkHeightsMaverick());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(maverick.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Park Heights Maverick cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        Permanent maverick = addCreatureReady(player1, new ParkHeightsMaverick());
        maverick.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(bears), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Combat damage proliferates")
    void combatDamageProliferates() {
        harness.setLife(player2, 10);
        Permanent maverick = addCreatureReady(player1, new ParkHeightsMaverick());
        maverick.setAttacking(true);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("When it dies, Park Heights Maverick proliferates")
    void deathProliferates() {
        Permanent maverick = addCreatureReady(player1, new ParkHeightsMaverick());
        maverick.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void dethroneDoesNotTriggerWhenControllerHasMoreLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        Permanent maverick = addCreatureReady(player1, new ParkHeightsMaverick());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(maverick.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void dethroneTriggersWhenDefenderHasStrictlyMostLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent maverick = addCreatureReady(player1, new ParkHeightsMaverick());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(maverick.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canBeBlockedByCreatureWhoseCountersRaisePowerToThree() {
        Permanent maverick = addCreatureReady(player1, new ParkHeightsMaverick());
        maverick.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.getBlockingTargets()).contains(0);
    }

    @Test
    void combatDamageCanProliferateOpponentsPermanentAndPlayer() {
        Permanent maverick = addCreatureReady(player1, new ParkHeightsMaverick());
        maverick.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), player2.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void combatDamageCanProliferateItsOwnCounter() {
        Permanent maverick = addCreatureReady(player1, new ParkHeightsMaverick());
        maverick.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        maverick.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(maverick.getId()));

        assertThat(maverick.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void combatDamageAllowsChoosingNoPermanentsToProliferate() {
        Permanent maverick = addCreatureReady(player1, new ParkHeightsMaverick());
        maverick.setAttacking(true);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
