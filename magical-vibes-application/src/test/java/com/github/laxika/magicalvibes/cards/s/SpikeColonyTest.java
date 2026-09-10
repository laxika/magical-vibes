package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MoxDiamond;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeColony.class, SpikeBreeder.class, MoxDiamond.class})
class SpikeColonyTest extends BaseCardTest {

    private Permanent castColony() {
        harness.castFromHand(player1, new SpikeColony(), "{4}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Spike Colony");
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("Enters with four +1/+1 counters, making it a 4/4")
    void entersWithFourCounters() {
        Permanent colony = castColony();

        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removes a counter to put one on target creature")
    void putsCounterOnTargetCreature() {
        Permanent colony = castColony();
        Permanent target = harness.enterBattlefieldAndReturn(player1, new SpikeBreeder());
        int initialTargetCounters = target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(colony), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(initialTargetCounters + 1);
    }

    @Test
    @DisplayName("Can put a counter on an opponent's creature")
    void putsCounterOnOpponentsCreature() {
        Permanent colony = castColony();
        Permanent target = harness.enterBattlefieldAndReturn(player2, new SpikeBreeder());
        int initialTargetCounters = target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(colony), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(initialTargetCounters + 1);
    }

    @Test
    @DisplayName("Can activate twice without tapping")
    void canActivateTwiceWithoutTapping() {
        Permanent colony = castColony();
        Permanent target = harness.enterBattlefieldAndReturn(player1, new SpikeBreeder());
        int initialTargetCounters = target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(colony), 0, null, target.getId());
        harness.activateAbility(player1, indexOf(colony), 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(initialTargetCounters + 2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent colony = castColony();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MoxDiamond());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(colony), 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent colony = castColony();
        Permanent target = harness.enterBattlefieldAndReturn(player1, new SpikeBreeder());
        colony.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(colony), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
