package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerfulBroker.class, Forest.class})
class PowerfulBrokerTest extends BaseCardTest {

    @Test
    void addsAnotherCounterOfEachKindToTargetPermanent() {
        addCreatureReady(player1, new PowerfulBroker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.setCounterCount(CounterType.CHARGE, 2);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void addsAnotherCounterOfEachKindToTargetPlayer() {
        addCreatureReady(player1, new PowerfulBroker());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void addsAnotherRadCounterToTargetPlayer() {
        addCreatureReady(player1, new PowerfulBroker());
        gd.playerRadCounters.put(player2.getId(), 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void addsAnotherExperienceCounterToTargetPlayer() {
        addCreatureReady(player1, new PowerfulBroker());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void addsAnotherSparkCounterToTargetPlayer() {
        addCreatureReady(player1, new PowerfulBroker());
        gd.playerSparkCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerSparkCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void counterlessPermanentIsLegalAndReceivesNoCounters() {
        Permanent broker = addCreatureReady(player1, new PowerfulBroker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(broker.isTapped()).isTrue();
        harness.passBothPriorities();

        for (CounterType type : CounterType.values()) {
            assertThat(target.getCounterCount(type)).isZero();
        }
    }

    @Test
    void counterlessPlayerReceivesNoCounters() {
        addCreatureReady(player1, new PowerfulBroker());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerExperienceCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void usesCounterKindsPresentAtResolution() {
        addCreatureReady(player1, new PowerfulBroker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.CHARGE, 0);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent broker = addCreatureReady(player1, new PowerfulBroker());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(broker.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsTurn() {
        Permanent broker = addCreatureReady(player1, new PowerfulBroker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(broker.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileAnotherAbilityIsOnStack() {
        addCreatureReady(player1, new PowerfulBroker());
        Permanent secondBroker = addCreatureReady(player1, new PowerfulBroker());
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(secondBroker.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent broker = harness.addToBattlefieldAndReturn(player1, new PowerfulBroker());
        broker.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(broker.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
