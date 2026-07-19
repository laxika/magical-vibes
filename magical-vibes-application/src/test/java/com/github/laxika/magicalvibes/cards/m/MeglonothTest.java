package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeglonothTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking deals damage equal to power to the blocked creature's controller, not the creature")
    void blockingDealsPowerDamageToAttackerController() {
        Permanent meglonoth = addReadyMeglonoth(player2);
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(meglonoth.getId());

        harness.passBothPriorities();

        // 6 damage goes to the attacker's controller (a player) — the 2/2 attacker itself is unharmed.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 6);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Block trigger is non-targeting (cannot be fizzled)")
    void blockTriggerIsNonTargeting() {
        addReadyMeglonoth(player2);
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.isNonTargeting()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyMeglonoth(Player player) {
        Permanent perm = new Permanent(new Meglonoth());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
