package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavingDead.class, GrizzlyBears.class})
class RavingDeadTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, Raving Dead must attack the chosen opponent")
    void mustAttackChosenOpponent() {
        Permanent ravingDead = addReadyRavingDead(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(ravingDead.isMustAttackThisCombat()).isTrue();
        assertThat(ravingDead.getMustAttackTargetId()).isEqualTo(player2.getId());

        beginDeclareAttackers(player1);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Combat damage makes the damaged player lose half their life, rounded down")
    void combatDamageMakesPlayerLoseHalfLifeRoundedDown() {
        harness.setLife(player2, 23);
        Permanent ravingDead = addCreatureReady(player1, new RavingDead());
        ravingDead.setAttacking(true);

        resolveCombat();

        // Raving Dead deals 2 combat damage first: 23 -> 21.
        // Half of 21 rounded down is 10: 21 -> 11.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Raving Dead does not trigger when blocked")
    void doesNotTriggerWhenBlocked() {
        harness.setLife(player2, 22);
        Permanent ravingDead = addCreatureReady(player1, new RavingDead());
        ravingDead.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    private Permanent addReadyRavingDead(Player player) {
        Permanent ravingDead = new Permanent(new RavingDead());
        ravingDead.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ravingDead);
        return ravingDead;
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
