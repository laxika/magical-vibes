package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(worker.getEffectivePower()).isEqualTo(4);
        assertThat(worker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets two +1/+1 counters with both named Workers")
    void getsCountersWithWorkerAssembly() {
        Permanent worker = addReadyPowerPlantWorker();
        addNamedCreature(player1, "Mine Worker");
        addNamedCreature(player1, "Tower Worker");
        addThreeColorlessMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(worker.getEffectivePower()).isEqualTo(6);
        assertThat(worker.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Checks the named Workers when the ability resolves")
    void checksWorkerAssemblyAtResolution() {
        Permanent worker = addReadyPowerPlantWorker();
        addNamedCreature(player1, "Mine Worker");
        addNamedCreature(player1, "Tower Worker");
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
                .hasMessageContaining("no more than 1 times each turn");
    }

    private Permanent addReadyPowerPlantWorker() {
        return addCreatureReady(player1, new PowerPlantWorker());
    }

    private void addThreeColorlessMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addNamedCreature(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
