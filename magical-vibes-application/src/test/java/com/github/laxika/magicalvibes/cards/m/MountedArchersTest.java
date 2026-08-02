package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MountedArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability grants one additional block this turn")
    void grantsAdditionalBlock() {
        Permanent archers = addArchers();

        activate(archers);

        assertThat(archers.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated twice for two additional blocks")
    void grantsStack() {
        Permanent archers = addArchers();

        activate(archers);
        activate(archers);

        assertThat(archers.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mounted Archers blocks two attackers after activating once")
    void blocksTwoAttackers() {
        Permanent archers = addArchers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(archers);
        addAttacker();
        addAttacker();

        activate(archers);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ));

        assertThat(archers.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("The grant wears off at end of turn")
    void grantExpiresAtEndOfTurn() {
        Permanent archers = addArchers();
        activate(archers);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(archers.getAdditionalBlocksUntilEndOfTurn()).isZero();
    }

    private Permanent addArchers() {
        Permanent perm = new Permanent(new MountedArchers());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(perm);
        return perm;
    }

    private void activate(Permanent archers) {
        int idx = gd.playerBattlefields.get(player2.getId()).indexOf(archers);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, idx, null, null);
        harness.passBothPriorities();
    }

    private Permanent addAttacker() {
        Permanent atk = new Permanent(new GrizzlyBears());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        atk.setAttackTarget(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(atk);
        return atk;
    }
}
