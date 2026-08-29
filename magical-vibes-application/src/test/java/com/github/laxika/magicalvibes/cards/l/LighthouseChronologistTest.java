package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LighthouseChronologistTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Lighthouse Chronologist's stats at levels four and seven")
    void levelsUpAtThresholds() {
        Permanent chronologist = addCreatureReady(player1, new LighthouseChronologist());
        prepareForLeveling(player1, 7);

        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }
        assertThat(chronologist.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertStats(chronologist, 2, 4);

        for (int i = 0; i < 3; i++) {
            levelUp(player1);
        }
        assertThat(chronologist.getCounterCount(CounterType.LEVEL)).isEqualTo(7);
        assertStats(chronologist, 3, 5);
    }

    @Test
    @DisplayName("At level seven, Lighthouse Chronologist grants an extra turn after an opponent's end step")
    void grantsExtraTurnOnOpponentsEndStep() {
        Permanent chronologist = addCreatureReady(player1, new LighthouseChronologist());
        chronologist.setCounterCount(CounterType.LEVEL, 7);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentTurnIsExtraTurn).isTrue();
    }

    @Test
    @DisplayName("Lighthouse Chronologist does not grant an extra turn below level seven")
    void doesNotTriggerBelowLevelSeven() {
        Permanent chronologist = addCreatureReady(player1, new LighthouseChronologist());
        chronologist.setCounterCount(CounterType.LEVEL, 6);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
    }

    private void prepareForLeveling(Player player, int blueMana) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.BLUE, blueMana);
    }

    private void levelUp(Player player) {
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
