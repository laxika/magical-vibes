package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoxiteRefinery.class, MishrasBauble.class, GrizzlyBears.class})
class MoxiteRefineryTest extends BaseCardTest {

    @Test
    @DisplayName("Removes X counters and puts X charge counters on target artifact")
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
    @DisplayName("Puts X +1/+1 counters on target creature")
    void putsPlusOneCountersOnTargetCreature() {
        addReadyRefinery();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, 2, target.getId());
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The charge counter mode can't target a creature that isn't an artifact")
    void rejectsWrongTargetType() {
        addReadyRefinery();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The +1/+1 counter mode can target a creature an opponent controls")
    void canTargetOpponentsCreature() {
        addReadyRefinery();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        source.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, 1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The charge counter mode can target an artifact an opponent controls")
    void canTargetOpponentsArtifact() {
        addReadyRefinery();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());
        source.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 1, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(opponentArtifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("All X counters come from one permanent: they can't be split across two")
    void cannotSplitCountersAcrossPermanents() {
        addReadyRefinery();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.CHARGE, 1);
        second.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("X can be 0: the ability is activated without removing any counter")
    void xCanBeZero() {
        Permanent refinery = addReadyRefinery();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(refinery.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Can be activated only as a sorcery")
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
