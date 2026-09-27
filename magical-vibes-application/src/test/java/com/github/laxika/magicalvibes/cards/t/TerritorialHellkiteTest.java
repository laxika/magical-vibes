package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerritorialHellkite.class})
class TerritorialHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, Territorial Hellkite must attack an eligible opponent")
    void mustAttackEligibleOpponent() {
        Permanent hellkite = addReadyHellkite(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(hellkite.isMustAttackThisCombat()).isTrue();
        assertThat(hellkite.getMustAttackTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Territorial Hellkite taps when it attacked every opponent during its last combat")
    void tapsWhenNoOpponentIsEligible() {
        Permanent hellkite = addReadyHellkite(player1);
        hellkite.setAttacking(true);
        hellkite.setAttackTarget(player2.getId());
        hellkite.rollOverCombatAttackRecord();
        hellkite.clearCombatState();

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(hellkite.isTapped()).isTrue();
        assertThat(hellkite.isMustAttackThisCombat()).isFalse();
    }

    private Permanent addReadyHellkite(Player player) {
        return addCreatureReady(player, new TerritorialHellkite());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
