package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TornadoTest extends BaseCardTest {

    private void addMana(int green, int generic) {
        harness.addMana(player1, ManaColor.GREEN, green);
        harness.addMana(player1, ManaColor.COLORLESS, generic);
    }

    @Test
    @DisplayName("First activation costs no life, destroys the target and adds a velocity counter")
    void firstActivationCostsNoLife() {
        Permanent tornado = harness.addToBattlefieldAndReturn(player1, new Tornado());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        addMana(1, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(tornado.getCounterCount(CounterType.VELOCITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Life cost scales with the velocity counters already on Tornado")
    void lifeCostScalesWithVelocityCounters() {
        Permanent tornado = harness.addToBattlefieldAndReturn(player1, new Tornado());
        tornado.setCounterCount(CounterType.VELOCITY, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        addMana(1, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(tornado.getCounterCount(CounterType.VELOCITY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activation is rejected when the controller can't pay the life cost")
    void rejectsActivationWithoutEnoughLife() {
        Permanent tornado = harness.addToBattlefieldAndReturn(player1, new Tornado());
        tornado.setCounterCount(CounterType.VELOCITY, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 5);
        addMana(1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("The ability can only be activated once each turn")
    void onlyOnceEachTurn() {
        harness.addToBattlefieldAndReturn(player1, new Tornado());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        addMana(2, 4);

        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, second.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second);
    }
}
