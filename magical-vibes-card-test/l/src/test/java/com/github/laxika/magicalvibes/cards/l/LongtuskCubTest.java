package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongtuskCub.class})
class LongtuskCubTest extends BaseCardTest {

    @Test
    void gainsTwoEnergyWhenItDealsCombatDamageToAPlayer() {
        Permanent cub = addCreatureReady(player1, new LongtuskCub());
        cub.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void paysTwoEnergyToPutACounterOnItself() {
        Permanent cub = addCreatureReady(player1, new LongtuskCub());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutTwoEnergyCounters() {
        addCreatureReady(player1, new LongtuskCub());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two energy counters");
    }

    @Test
    void gainsExactlyTwoEnergyRegardlessOfCombatDamageAmount() {
        Permanent cub = addCreatureReady(player1, new LongtuskCub());
        cub.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        cub.setAttacking(true);
        gd.playerEnergyCounters.put(player1.getId(), 1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void combatDamageToACreatureDoesNotGrantEnergy() {
        addCreatureReady(player1, new LongtuskCub());
        addCreatureReady(player2, new LongtuskCub());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent cub = harness.addToBattlefieldAndReturn(player1, new LongtuskCub());
        cub.setSummoningSick(true);
        cub.tap();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void repeatedActivationsPayEnergyImmediatelyAndResolveSeparately() {
        Permanent cub = addCreatureReady(player1, new LongtuskCub());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
