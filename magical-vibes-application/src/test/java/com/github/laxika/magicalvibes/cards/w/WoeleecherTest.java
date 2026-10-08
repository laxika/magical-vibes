package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BarrentonCragtreads;
import com.github.laxika.magicalvibes.cards.b.BlightSickle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Woeleecher.class, BarrentonCragtreads.class, BlightSickle.class})
class WoeleecherTest extends BaseCardTest {

    @Test
    @DisplayName("Removes a -1/-1 counter from target creature and gains 2 life")
    void removesCounterAndGainsLife() {
        addCreatureReady(player1, new Woeleecher());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BarrentonCragtreads());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);

        // Barrenton Cragtreads (3/3) survives with two -1/-1 counters (1/1); one is removed.
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The counter comes off the target, not off Woeleecher itself")
    void counterComesOffTheTargetNotTheSource() {
        Permanent woeleecher = addCreatureReady(player1, new Woeleecher());
        // Woeleecher is the ability's source permanent, so a source/target mix-up is observable:
        // it carries its own -1/-1 counter (3/5 becomes 2/4, survives).
        woeleecher.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BarrentonCragtreads());
        harness.addMana(player1, ManaColor.WHITE, 1);

        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(woeleecher.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        addCreatureReady(player1, new Woeleecher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarrentonCragtreads());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("No life gained when the target has no -1/-1 counter")
    void noLifeWhenNoCounter() {
        addCreatureReady(player1, new Woeleecher());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BarrentonCragtreads());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Activation taps Woeleecher")
    void activationTapsWoeleecher() {
        Permanent woeleecher = addCreatureReady(player1, new Woeleecher());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BarrentonCragtreads());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(woeleecher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new Woeleecher());
        Permanent sickle = harness.addToBattlefieldAndReturn(player2, new BlightSickle());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, sickle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new Woeleecher());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BarrentonCragtreads());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can remove its own counter and gain life")
    void canTargetItself() {
        Permanent woeleecher = addCreatureReady(player1, new Woeleecher());
        woeleecher.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, woeleecher.getId());
        harness.passBothPriorities();

        assertThat(woeleecher.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("No life gained if the last counter disappears before resolution")
    void noLifeWhenCounterDisappearsBeforeResolution() {
        addCreatureReady(player1, new Woeleecher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarrentonCragtreads());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent woeleecher = harness.addToBattlefieldAndReturn(player1, new Woeleecher());
        woeleecher.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, woeleecher.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(woeleecher.isTapped()).isFalse();
    }
}
