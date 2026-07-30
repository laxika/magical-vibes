package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GiantAlbatrossTest extends BaseCardTest {

    /**
     * Blocks a lethal 5/5 Grizzly Bears with Giant Albatross and advances to combat damage so the
     * Albatross dies to that creature's damage, then resolves the death trigger up to its may-pay
     * prompt.
     */
    private void killByBearsUntilMayPrompt() {
        Permanent albatross = new Permanent(new GiantAlbatross());
        albatross.setSummoningSick(false);
        albatross.setBlocking(true);
        albatross.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(albatross);

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

        harness.passBothPriorities(); // advance to combat damage → Giant Albatross dies
        harness.passBothPriorities(); // resolve death trigger → may-pay prompt

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Pay {1}{U}; the damaging creature's controller declines 2 life, so it is destroyed")
    void payThenDamagerControllerDeclines() {
        killByBearsUntilMayPrompt();

        harness.assertInGraveyard(player1, "Giant Albatross");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Pay {1}{U}; the damaging creature's controller pays 2 life to save it")
    void payThenDamagerControllerPaysLife() {
        killByBearsUntilMayPrompt();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Decline {1}{U}: nothing is destroyed")
    void declinePayingLeavesDamagerAlone() {
        killByBearsUntilMayPrompt();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A controller who can't pay 2 life gets no choice — the creature is destroyed")
    void controllerWhoCannotPayLosesTheCreature() {
        killByBearsUntilMayPrompt();
        gd.playerLifeTotals.put(player2.getId(), 1);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 1);
    }
}
