package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FogPatchTest extends BaseCardTest {

    private void giveSpell() {
        harness.setHand(player2, List.of(new FogPatch()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }

    @Test
    void makesAllUnblockedAttackersBlockedAndDealsNoCombatDamage() {
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(firstAttacker.isBlockedWithoutBlockers()).isTrue();
        assertThat(secondAttacker.isBlockedWithoutBlockers()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    void leavesAlreadyBlockedAttackersBlockedByTheirBlockers() {
        Permanent blockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unblockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0, 1));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        giveSpell();
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(blockedAttacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(unblockedAttacker.isBlockedWithoutBlockers()).isTrue();
    }

    @Test
    void cannotBeCastOutsideDeclareBlockersStep() {
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
