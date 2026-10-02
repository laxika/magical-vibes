package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Aeromunculus.class})
class AeromunculusTest extends BaseCardTest {

    @Test
    void adaptPutsOnePlusOneCounterOnAeromunculus() {
        Permanent aeromunculus = addAeromunculus();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(aeromunculus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adaptCanBeActivatedWithPlusOneCounter() {
        Permanent aeromunculus = addAeromunculus();
        aeromunculus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(aeromunculus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adaptChecksForCountersOnResolution() {
        Permanent aeromunculus = addAeromunculus();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        aeromunculus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(aeromunculus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void stackedAdaptActivationsPutOnlyOneCounter() {
        Permanent aeromunculus = addAeromunculus();
        addAdaptMana();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(aeromunculus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(aeromunculus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adaptPutsCounterIfExistingCounterIsRemovedBeforeResolution() {
        Permanent aeromunculus = addAeromunculus();
        aeromunculus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        aeromunculus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(aeromunculus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adaptCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent aeromunculus = harness.addToBattlefieldAndReturn(player1, new Aeromunculus());
        aeromunculus.setSummoningSick(true);
        aeromunculus.setTapped(true);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(aeromunculus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(aeromunculus.isTapped()).isTrue();
    }

    private Permanent addAeromunculus() {
        return addCreatureReady(player1, new Aeromunculus());
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
