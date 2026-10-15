package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoardedWindow.class, GrizzlyBears.class, LilianaOfTheVeil.class})
class BoardedWindowTest extends BaseCardTest {

    private Permanent addWindow() {
        return harness.addToBattlefieldAndReturn(player1, new BoardedWindow());
    }

    /** Puts an attacking 2/2 on player2's battlefield attacking {@code attackTarget}. */
    private Permanent addAttacker(UUID attackTarget) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(attackTarget);
        return attacker;
    }

    private void advanceToEndStepAndResolve(UUID activePlayerId) {
        harness.forceActivePlayer(activePlayerId.equals(player1.getId()) ? player1 : player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creatures attacking its controller get -1/-0")
    void weakensCreaturesAttackingController() {
        addWindow();
        Permanent attacker = addAttacker(player1.getId());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures attacking its controller's planeswalker are unaffected")
    void ignoresCreaturesAttackingPlaneswalker() {
        addWindow();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new LilianaOfTheVeil());
        Permanent attacker = addAttacker(planeswalker.getId());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures that are not attacking are unaffected")
    void ignoresNonAttackingCreatures() {
        addWindow();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles itself at end step when its controller was dealt 4 damage this turn")
    void exilesItselfAfterFourDamage() {
        Permanent window = addWindow();
        gd.recordDamageToPlayer(player1.getId(), 4);

        advanceToEndStepAndResolve(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(window);
    }

    @Test
    @DisplayName("Stays on the battlefield when its controller was dealt only 3 damage")
    void staysBelowThreshold() {
        Permanent window = addWindow();
        gd.recordDamageToPlayer(player1.getId(), 3);

        advanceToEndStepAndResolve(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(window);
    }

    @Test
    @DisplayName("Damage dealt to the opponent does not exile it")
    void ignoresDamageToOpponent() {
        Permanent window = addWindow();
        gd.recordDamageToPlayer(player2.getId(), 5);

        advanceToEndStepAndResolve(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(window);
    }

    @Test
    @DisplayName("Exiles itself at its controller's own end step too")
    void exilesOnControllerEndStep() {
        Permanent window = addWindow();
        gd.recordDamageToPlayer(player1.getId(), 6);

        advanceToEndStepAndResolve(player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(window);
    }

    @Test
    @DisplayName("Multiple Windows each weaken attackers")
    void multipleWindowsStackTheirPenalty() {
        addWindow();
        addWindow();
        Permanent attacker = addAttacker(player1.getId());

        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The penalty ends when a creature stops attacking")
    void penaltyEndsWhenAttackEnds() {
        addWindow();
        Permanent attacker = addAttacker(player1.getId());
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);

        attacker.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage from multiple events counts even before the Window enters")
    void countsCumulativeDamageBeforeEntering() {
        gd.recordDamageToPlayer(player1.getId(), 2);
        gd.recordDamageToPlayer(player1.getId(), 2);
        Permanent window = addWindow();

        advanceToEndStepAndResolve(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(window);
        assertThat(gd.exiledCards).anySatisfy(entry ->
                assertThat(entry.card()).isSameAs(window.getCard()));
    }

    @Test
    @DisplayName("Life loss without damage does not cause exile")
    void lifeLossDoesNotMeetDamageThreshold() {
        Permanent window = addWindow();
        harness.setLife(player1, 16);

        advanceToEndStepAndResolve(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(window);
    }

    @Test
    @DisplayName("Reaching the damage threshold after the end step begins does not trigger")
    void damageAfterEndStepBeginsDoesNotTrigger() {
        Permanent window = addWindow();
        gd.recordDamageToPlayer(player1.getId(), 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        gd.recordDamageToPlayer(player1.getId(), 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(window);
    }
}
