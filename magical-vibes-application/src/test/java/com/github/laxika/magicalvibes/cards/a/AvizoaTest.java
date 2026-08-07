package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvizoaTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives +2/+2 until end of turn")
    void pumpsUntilEndOfTurn() {
        Permanent avizoa = addCreatureReady(player1, new Avizoa());

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();

        assertThat(avizoa.getEffectivePower()).isEqualTo(4);
        assertThat(avizoa.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(avizoa.getEffectivePower()).isEqualTo(2);
        assertThat(avizoa.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability queues a skip of the controller's next untap step")
    void queuesSkipNextUntapStep() {
        Permanent avizoa = addCreatureReady(player1, new Avizoa());

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();

        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Queued skip leaves the controller's permanents tapped on their next turn")
    void skipsNextUntapStep() {
        Permanent avizoa = addCreatureReady(player1, new Avizoa());

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();
        avizoa.tap();

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(avizoa.isTapped()).isTrue();
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);

        advanceTurn();
        advanceTurn();
        assertThat(avizoa.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate more than once each turn")
    void onlyOncePerTurn() {
        Permanent avizoa = addCreatureReady(player1, new Avizoa());

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(avizoa), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
