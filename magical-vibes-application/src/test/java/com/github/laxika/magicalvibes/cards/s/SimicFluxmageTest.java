package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimicFluxmageTest extends BaseCardTest {

    @Test
    @DisplayName("Moves a +1/+1 counter from itself onto target creature")
    void movesCounterFromSourceToTarget() {
        Permanent fluxmage = addReadyFluxmage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        addReadyFluxmage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent fluxmage = findPermanent(player1, "Simic Fluxmage");
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
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyFluxmage() {
        Permanent fluxmage = harness.addToBattlefieldAndReturn(player1, new SimicFluxmage());
        fluxmage.setSummoningSick(false);
        return fluxmage;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
