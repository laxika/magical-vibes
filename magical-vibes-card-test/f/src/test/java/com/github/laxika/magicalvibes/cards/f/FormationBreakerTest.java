package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

@CardUsed({FormationBreaker.class, Forest.class, GrizzlyBears.class, SuntailHawk.class})
class FormationBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by a creature with less power")
    void cannotBeBlockedByLowerPower() {
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        breaker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(breaker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("Can be blocked by a creature with equal power")
    void canBeBlockedByEqualPower() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        breaker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(breaker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gets +1/+2 while you control a creature with any counter")
    void getsBoostWhileControllingCreatureWithCounter() {
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.getEffectivePower(gd, breaker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, breaker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A counter on an opponent's creature does not provide the boost")
    void opponentCounterDoesNotProvideBoost() {
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.getEffectivePower(gd, breaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, breaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("A counter on Formation Breaker itself enables the bonus and removing it ends the bonus")
    void ownCounterEnablesBonusUntilRemoved() {
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        breaker.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.getEffectivePower(gd, breaker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, breaker)).isEqualTo(3);

        breaker.setCounterCount(CounterType.CHARGE, 0);

        assertThat(gqs.getEffectivePower(gd, breaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, breaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple creatures and counters enable the bonus only once")
    void multipleCounterBearersDoNotMultiplyBonus() {
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        Permanent other = addCreatureReady(player1, new FormationBreaker());
        breaker.setCounterCount(CounterType.CHARGE, 2);
        other.setCounterCount(CounterType.CHARGE, 3);

        assertThat(gqs.getEffectivePower(gd, breaker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, breaker)).isEqualTo(3);

        breaker.setCounterCount(CounterType.CHARGE, 0);
        assertThat(gqs.getEffectivePower(gd, breaker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, breaker)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(other);
        assertThat(gqs.getEffectivePower(gd, breaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, breaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("The conditional power bonus raises the minimum blocker power")
    void boostedAttackerRejectsTwoPowerBlocker() {
        Permanent blocker = addCreatureReady(player2, new FormationBreaker());
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        breaker.setCounterCount(CounterType.CHARGE, 1);
        breaker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Blocking compares both creatures' effective power including counters")
    void blockerWithEnoughEffectivePowerCanBlock() {
        Permanent blocker = addCreatureReady(player2, new FormationBreaker());
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        breaker.setCounterCount(CounterType.CHARGE, 1);
        breaker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
    @Test
    @DisplayName("A counter on a noncreature permanent does not enable the bonus")
    void noncreatureCounterDoesNotProvideBoost() {
        Permanent breaker = addCreatureReady(player1, new FormationBreaker());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.getEffectivePower(gd, breaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, breaker)).isEqualTo(1);
    }
}
