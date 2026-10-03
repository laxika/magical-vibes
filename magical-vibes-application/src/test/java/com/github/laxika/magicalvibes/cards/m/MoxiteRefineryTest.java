package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoxiteRefinery.class, MishrasBauble.class, GrizzlyBears.class})
class MoxiteRefineryTest extends BaseCardTest {

    @Test
    void removesAnyCountersAndPutsChargeCountersOnTargetArtifact() {
        Permanent refinery = addReadyRefinery();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        source.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(refinery.isTapped()).isTrue();
    }

    @Test
    void putsPlusOneCountersOnTargetCreature() {
        addReadyRefinery();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void rejectsWrongTargetTypeAndOpponentsPermanent() {
        addReadyRefinery();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        source.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, 1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void isSorcerySpeedOnly() {
        addReadyRefinery();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        source.setCounterCount(CounterType.CHARGE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyRefinery() {
        return harness.addToBattlefieldAndReturn(player1, new MoxiteRefinery());
    }
}
