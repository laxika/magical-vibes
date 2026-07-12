package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GiantBadgerTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking triggers +2/+2 until end of turn")
    void blockingTriggersBoost() {
        Permanent badger = addReadyBadger(player2);
        addReadyAttacker(player1, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Block trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(badger.getPowerModifier()).isEqualTo(2);
        assertThat(badger.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, badger)).isEqualTo(4);   // 2 base + 2
        assertThat(gqs.getEffectiveToughness(gd, badger)).isEqualTo(4); // 2 base + 2
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent badger = addReadyBadger(player2);
        addReadyAttacker(player1, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(badger.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.CLEANUP);
        badger.resetModifiers();

        assertThat(badger.getPowerModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, badger)).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addReadyBadger(Player player) {
        Permanent perm = new Permanent(new GiantBadger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyAttacker(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
