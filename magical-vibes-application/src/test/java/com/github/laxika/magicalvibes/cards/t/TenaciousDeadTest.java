package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenaciousDeadTest extends BaseCardTest {

    /**
     * Puts Tenacious Dead on the battlefield blocking a lethal 5/5 attacker, advances to combat
     * damage so it dies, then resolves the queued death trigger up to the may-pay prompt.
     */
    private void killInCombatUntilMayPrompt() {
        Permanent dead = new Permanent(new TenaciousDead());
        dead.setSummoningSick(false);
        dead.setBlocking(true);
        dead.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(dead);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(5);
        bears.setToughness(5);
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to combat damage → Tenacious Dead dies
        harness.passBothPriorities(); // resolve death trigger → may-pay prompt
    }

    @Test
    @DisplayName("Dies, pay {1}{B}, returns to the battlefield tapped")
    void diesPayReturnsTapped() {
        killInCombatUntilMayPrompt();

        harness.assertInGraveyard(player1, "Tenacious Dead");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Tenacious Dead");
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Tenacious Dead".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Dies, decline paying {1}{B}, stays in the graveyard")
    void diesDeclineStaysInGraveyard() {
        killInCombatUntilMayPrompt();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Tenacious Dead");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> "Tenacious Dead".equals(permanent.getCard().getName()));
    }
}
