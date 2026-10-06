package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavingDead.class})
class RavingDeadTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, Raving Dead must attack the chosen opponent")
    void mustAttackChosenOpponent() {
        Permanent ravingDead = addCreatureReady(player1, new RavingDead());

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();

        assertThat(ravingDead.isMustAttackThisCombat()).isTrue();
        assertThat(ravingDead.getMustAttackTargetId()).isEqualTo(player2.getId());
        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
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
        resolveAllTriggers();

        // Combat damage leaves 21 life; losing 10 leaves 11.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Raving Dead does not trigger when blocked")
    void doesNotTriggerWhenBlocked() {
        harness.setLife(player2, 22);
        Permanent ravingDead = addCreatureReady(player1, new RavingDead());
        ravingDead.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RavingDead());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    void tappedCreatureIsNotRequiredToAttack() {
        Permanent ravingDead = addCreatureReady(player1, new RavingDead());
        ravingDead.tap();

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    void summoningSickCreatureIsNotRequiredToAttack() {
        harness.addToBattlefield(player1, new RavingDead());

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    void doesNotTriggerAtBeginningOfOpponentsCombat() {
        Permanent ravingDead = addCreatureReady(player1, new RavingDead());

        advanceToBeginningOfCombat(player2);
        resolveAllTriggers();

        assertThat(ravingDead.isMustAttackThisCombat()).isFalse();
        assertThat(ravingDead.getMustAttackTargetId()).isNull();
    }

    @Test
    void lifeLossUsesLifeTotalAtResolutionEvenIfSourceLeaves() {
        harness.setLife(player2, 22);
        Permanent ravingDead = addCreatureReady(player1, new RavingDead());
        ravingDead.setAttacking(true);

        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(ravingDead);
        gd.playerGraveyards.get(player1.getId()).add(ravingDead.getCard());
        harness.setLife(player2, 15);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    void combatDamageLeavesOneLifeWithoutAdditionalLifeLoss() {
        harness.setLife(player2, 3);
        Permanent ravingDead = addCreatureReady(player1, new RavingDead());
        ravingDead.setAttacking(true);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(1);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
