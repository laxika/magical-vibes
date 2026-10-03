package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarnivorousMossBeast.class})
class CarnivorousMossBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts a +1/+1 counter on it, growing it permanently")
    void activationAddsCounter() {
        Permanent beast = addBeast();
        harness.addMana(player1, ManaColor.GREEN, 7);

        activateAndResolve();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(6);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly, stacking counters")
    void repeatedActivationsStack() {
        Permanent beast = addBeast();
        harness.addMana(player1, ManaColor.GREEN, 14);

        activateAndResolve();
        activateAndResolve();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(7);
    }

    @Test
    @DisplayName("A tapped, summoning-sick beast can activate with five generic and two green mana")
    void tappedSummoningSickBeastCanActivate() {
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new CarnivorousMossBeast());
        beast.setSummoningSick(true);
        beast.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        activateAndResolve();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(beast.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Seven mana with only one green cannot pay the activation cost")
    void requiresTwoGreenMana() {
        Permanent beast = addBeast();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Six mana cannot pay the seven-mana activation cost")
    void requiresSevenMana() {
        Permanent beast = addBeast();
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter is placed on the activating beast only when the ability resolves")
    void counterWaitsForResolutionAndOnlyAffectsSource() {
        Permanent beast = addBeast();
        Permanent otherBeast = addBeast();
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherBeast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherBeast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addBeast() {
        return addCreatureReady(player1, new CarnivorousMossBeast());
    }
}
