package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MineWorker;
import com.github.laxika.magicalvibes.cards.t.TowerWorker;
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

@CardUsed({PowerPlantWorker.class, MineWorker.class, TowerWorker.class})
class PowerPlantWorkerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 until end of turn without the other Workers")
    void getsTemporaryBoostWithoutWorkerAssembly() {
        Permanent worker = addReadyPowerPlantWorker();
        addThreeColorlessMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(worker.getEffectivePower()).isEqualTo(6);
        assertThat(worker.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(worker.getEffectivePower()).isEqualTo(4);
        assertThat(worker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets two +1/+1 counters with both named Workers")
    void getsCountersWithWorkerAssembly() {
        Permanent worker = addReadyPowerPlantWorker();
        harness.addToBattlefield(player1, new MineWorker());
        harness.addToBattlefield(player1, new TowerWorker());
        addThreeColorlessMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(worker.getEffectivePower()).isEqualTo(6);
        assertThat(worker.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(worker.getEffectivePower()).isEqualTo(6);
        assertThat(worker.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Checks the named Workers when the ability resolves")
    void checksWorkerAssemblyAtResolution() {
        Permanent worker = addReadyPowerPlantWorker();
        harness.addToBattlefield(player1, new MineWorker());
        harness.addToBattlefield(player1, new TowerWorker());
        addThreeColorlessMana();

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Tower Worker"));
        harness.passBothPriorities();

        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(worker.getEffectivePower()).isEqualTo(6);
        assertThat(worker.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void canBeActivatedOnlyOnceEachTurn() {
        addReadyPowerPlantWorker();
        addThreeColorlessMana();
        addThreeColorlessMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    private Permanent addReadyPowerPlantWorker() {
        return addCreatureReady(player1, new PowerPlantWorker());
    }

    @Test
    void opponentWorkersDoNotEnableCounters() {
        Permanent worker = addReadyPowerPlantWorker();
        harness.addToBattlefield(player2, new MineWorker());
        harness.addToBattlefield(player2, new TowerWorker());
        addThreeColorlessMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(worker.getEffectivePower()).isEqualTo(6);
        assertThat(worker.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    void eitherWorkerAloneDoesNotEnableCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PowerPlantWorker());
        harness.addToBattlefield(player1, new MineWorker());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PowerPlantWorker());
        harness.addToBattlefield(player2, new TowerWorker());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getEffectivePower()).isEqualTo(6);
        assertThat(second.getEffectivePower()).isEqualTo(6);
    }

    @Test
    void workersEnteringBeforeResolutionEnableCounters() {
        Permanent worker = harness.addToBattlefieldAndReturn(player1, new PowerPlantWorker());
        addThreeColorlessMana();

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new MineWorker());
        harness.addToBattlefield(player1, new TowerWorker());
        harness.passBothPriorities();

        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(worker.getEffectivePower()).isEqualTo(6);
        assertThat(worker.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    void cannotActivateAgainWhileFirstActivationIsOnStack() {
        addReadyPowerPlantWorker();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    void canActivateAgainOnOpponentsTurn() {
        Permanent worker = addReadyPowerPlantWorker();
        addThreeColorlessMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        addThreeColorlessMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(worker.getEffectivePower()).isEqualTo(6);
        assertThat(worker.getEffectiveToughness()).isEqualTo(6);
        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addThreeColorlessMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

}
