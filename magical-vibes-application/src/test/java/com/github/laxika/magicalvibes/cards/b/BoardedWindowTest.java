package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BoardedWindowTest extends BaseCardTest {

    private Permanent addWindow() {
        Permanent window = new Permanent(new BoardedWindow());
        gd.playerBattlefields.get(player1.getId()).add(window);
        return window;
    }

    /** Puts an attacking 2/2 on player2's battlefield attacking {@code attackTarget}. */
    private Permanent addAttacker(UUID attackTarget) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(attackTarget);
        gd.playerBattlefields.get(player2.getId()).add(attacker);
        return attacker;
    }

    private void advanceToEndStepAndResolve(UUID activePlayerId) {
        harness.forceActivePlayer(activePlayerId.equals(player1.getId()) ? player1 : player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
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
    @DisplayName("Creatures attacking another player are unaffected")
    void ignoresCreaturesAttackingSomeoneElse() {
        addWindow();
        Permanent attacker = addAttacker(player2.getId());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures that are not attacking are unaffected")
    void ignoresNonAttackingCreatures() {
        addWindow();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

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
}
