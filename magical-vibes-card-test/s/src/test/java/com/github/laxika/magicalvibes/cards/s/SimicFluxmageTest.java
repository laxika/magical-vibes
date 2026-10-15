package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrilledOculus;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimicFluxmage.class, FrilledOculus.class, SimicKeyrune.class, HardenedScales.class,
        Shambleshark.class})
class SimicFluxmageTest extends BaseCardTest {

    @Test
    @DisplayName("Moves a +1/+1 counter from itself onto target creature")
    void movesCounterFromSourceToTarget() {
        Permanent fluxmage = addReadyFluxmage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrilledOculus());
        fluxmage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(fluxmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fluxmage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void targetsOpponentsCreature() {
        Permanent fluxmage = addReadyFluxmage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FrilledOculus());
        fluxmage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(fluxmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyFluxmage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SimicKeyrune());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void targetingItselfDoesNotPlaceCountersEvenWithHardenedScales() {
        Permanent fluxmage = addReadyFluxmage();
        harness.addToBattlefield(player1, new HardenedScales());
        fluxmage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, fluxmage.getId());
        harness.passBothPriorities();

        assertThat(fluxmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fluxmage.isTapped()).isTrue();
    }

    @Test
    void canActivateWithoutACounterButDoesNotGiveTargetOne() {
        Permanent fluxmage = addReadyFluxmage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrilledOculus());
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(fluxmage.isTapped()).isTrue();
    }

    @Test
    void movesOnlyOneOfMultipleCounters() {
        Permanent fluxmage = addReadyFluxmage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrilledOculus());
        fluxmage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(fluxmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void evolvesWhenAnAllyHasGreaterToughnessButEqualPower() {
        Permanent fluxmage = addReadyFluxmage();
        harness.enterBattlefieldAndReturn(player1, new FrilledOculus());
        resolveAllTriggers();

        assertThat(fluxmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void evolvesWhenAnAllyHasGreaterPowerButLowerToughness() {
        Permanent fluxmage = addReadyFluxmage();
        harness.enterBattlefieldAndReturn(player1, new Shambleshark());
        resolveAllTriggers();

        assertThat(fluxmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotEvolveWhenNeitherStatIsGreater() {
        Permanent fluxmage = addReadyFluxmage();
        fluxmage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.enterBattlefieldAndReturn(player1, new FrilledOculus());
        resolveAllTriggers();

        assertThat(fluxmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotEvolveForOpponentsCreature() {
        Permanent fluxmage = addReadyFluxmage();
        harness.enterBattlefieldAndReturn(player2, new FrilledOculus());
        resolveAllTriggers();

        assertThat(fluxmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void evolveRechecksStatsOnResolution() {
        Permanent fluxmage = addReadyFluxmage();
        harness.enterBattlefieldAndReturn(player1, new FrilledOculus());
        fluxmage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(fluxmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyFluxmage() {
        return addCreatureReady(player1, new SimicFluxmage());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
