package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    @DisplayName("The last controller combat remains relevant after an opponent's combat")
    void remembersAttackAcrossOpponentsCombat() {
        Permanent hellkite = addReadyHellkite(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(hellkite.isTapped()).isTrue();
        assertThat(hellkite.isMustAttackThisCombat()).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent hellkite = addReadyHellkite(player1);

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(hellkite.isTapped()).isFalse();
        assertThat(hellkite.isMustAttackThisCombat()).isFalse();
    }

    @Test
    @DisplayName("A tapped Hellkite still chooses an opponent")
    void choosesOpponentEvenWhenTapped() {
        Permanent hellkite = addReadyHellkite(player1);
        hellkite.tap();

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(hellkite.isTapped()).isTrue();
        assertThat(hellkite.getMustAttackTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("An opponent is eligible again after a controller combat without an attack")
    void opponentEligibleAfterSkippingCombat() {
        Permanent hellkite = addReadyHellkite(player1);
        hellkite.setAttacking(true);
        hellkite.setAttackTarget(player2.getId());
        hellkite.rollOverCombatAttackRecord();
        hellkite.clearCombatState();

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        hellkite.untap();
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(hellkite.isTapped()).isFalse();
        assertThat(hellkite.isMustAttackThisCombat()).isTrue();
        assertThat(hellkite.getMustAttackTargetId()).isEqualTo(player2.getId());
    }

    private Permanent addReadyHellkite(Player player) {
        return addCreatureReady(player, new TerritorialHellkite());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
