package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LuXunScholarGeneralTest extends BaseCardTest {

    // ===== Damage-to-player trigger =====

    @Test
    @DisplayName("Dealing combat damage to a player presents the may-draw choice")
    void combatDamagePresentsMayChoice() {
        Permanent luXun = addReadyCreature(player1, new LuXunScholarGeneral());
        luXun.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may-draw draws a card")
    void acceptingMayDrawsCard() {
        Permanent luXun = addReadyCreature(player1, new LuXunScholarGeneral());
        luXun.setAttacking(true);

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the may-draw does not draw a card")
    void decliningMayDoesNotDraw() {
        Permanent luXun = addReadyCreature(player1, new LuXunScholarGeneral());
        luXun.setAttacking(true);

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when blocked and no damage reaches the player")
    void noTriggerWhenBlocked() {
        Permanent luXun = addReadyCreature(player1, new LuXunScholarGeneral());
        luXun.setAttacking(true);

        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Defender takes combat damage regardless of the may choice")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent luXun = addReadyCreature(player1, new LuXunScholarGeneral());
        luXun.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        // Lu Xun is 1/3, deals 1 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
