package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MerfolkSeerTest extends BaseCardTest {

    /**
     * Puts Merfolk Seer on the battlefield blocking a lethal 5/5 attacker, advances to combat
     * damage so it dies, then resolves the queued death trigger up to the may-pay prompt.
     */
    private void killInCombatUntilMayPrompt() {
        Permanent seer = new Permanent(new MerfolkSeer());
        seer.setSummoningSick(false);
        seer.setBlocking(true);
        seer.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(seer);

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

        harness.passBothPriorities(); // advance to combat damage → Merfolk Seer dies
        harness.passBothPriorities(); // resolve death trigger → may-pay prompt
    }

    @Test
    @DisplayName("Dies, pay {1}{U}, draws a card")
    void diesPayDrawsCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killInCombatUntilMayPrompt();

        harness.assertInGraveyard(player1, "Merfolk Seer");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dies, decline paying {1}{U}, no card is drawn")
    void diesDeclineDrawsNothing() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killInCombatUntilMayPrompt();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Grizzly Bears");
    }
}
