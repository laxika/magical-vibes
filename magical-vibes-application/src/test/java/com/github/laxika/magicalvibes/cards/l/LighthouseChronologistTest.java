package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LighthouseChronologist.class})
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
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

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

    @Test
    @DisplayName("The extra turn trigger survives losing all level counters")
    void extraTurnTriggerSurvivesLosingLevelCounters() {
        Permanent chronologist = addCreatureReady(player1, new LighthouseChronologist());
        chronologist.setCounterCount(CounterType.LEVEL, 7);
        advanceToEndStep(player2);
        assertThat(gd.stack).hasSize(1);

        chronologist.setCounterCount(CounterType.LEVEL, 0);
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).hasSize(1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.currentTurnIsExtraTurn).isTrue();
    }

    @Test
    @DisplayName("Level seven does not trigger during its controller's end step")
    void doesNotTriggerOnControllersEndStep() {
        Permanent chronologist = addCreatureReady(player1, new LighthouseChronologist());
        chronologist.setCounterCount(CounterType.LEVEL, 7);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Level up is illegal during combat and on another player's turn")
    void levelUpRequiresControllersMainPhase() {
        Permanent chronologist = addCreatureReady(player1, new LighthouseChronologist());
        prepareForLeveling(player1, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chronologist.getCounterCount(CounterType.LEVEL)).isZero();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chronologist.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    @DisplayName("Level up cannot be activated in response to another level up")
    void levelUpRequiresEmptyStack() {
        Permanent chronologist = addCreatureReady(player1, new LighthouseChronologist());
        prepareForLeveling(player1, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(chronologist.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing level counters returns the creature to the lower level band")
    void statsFollowCurrentLevelCounters() {
        Permanent chronologist = addCreatureReady(player1, new LighthouseChronologist());
        int originalPower = gqs.getEffectivePower(gd, chronologist);
        int originalToughness = gqs.getEffectiveToughness(gd, chronologist);
        chronologist.setCounterCount(CounterType.LEVEL, 8);
        assertStats(chronologist, 3, 5);

        chronologist.setCounterCount(CounterType.LEVEL, 6);
        assertStats(chronologist, 2, 4);
        chronologist.setCounterCount(CounterType.LEVEL, 3);
        assertStats(chronologist, originalPower, originalToughness);
    }

    @Test
    @DisplayName("An extra turn is followed by the controller's regular turn")
    void regularTurnStillFollowsExtraTurn() {
        Permanent chronologist = addCreatureReady(player1, new LighthouseChronologist());
        chronologist.setCounterCount(CounterType.LEVEL, 7);
        advanceToEndStep(player2);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.currentTurnIsExtraTurn).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.currentTurnIsExtraTurn).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick creature can level up without tapping")
    void summoningSicknessDoesNotPreventLevelUp() {
        Permanent chronologist = harness.addToBattlefieldAndReturn(player1, new LighthouseChronologist());
        chronologist.setSummoningSick(true);
        prepareForLeveling(player1, 1);

        levelUp(player1);

        assertThat(chronologist.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(chronologist.isTapped()).isFalse();
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
