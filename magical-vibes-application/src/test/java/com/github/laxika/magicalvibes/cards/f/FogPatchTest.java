package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Cloudskate;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FogPatch.class, Mossdog.class, Cloudskate.class})
class FogPatchTest extends BaseCardTest {

    @Test
    void makesAllUnblockedAttackersBlockedAndDealsNoCombatDamage() {
        Permanent firstAttacker = addCreatureReady(player1, new Mossdog());
        Permanent secondAttacker = addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new Mossdog());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FogPatch(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(firstAttacker.isBlockedWithoutBlockers()).isTrue();
        assertThat(secondAttacker.isBlockedWithoutBlockers()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    void leavesAlreadyBlockedAttackersBlockedByTheirBlockers() {
        Permanent blockedAttacker = addCreatureReady(player1, new Mossdog());
        Permanent unblockedAttacker = addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new Mossdog());
        declareAttackers(List.of(0, 1));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FogPatch(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(blockedAttacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(unblockedAttacker.isBlockedWithoutBlockers()).isTrue();
    }

    @Test
    void makesAnAttackingCreatureWithEvasionBlockedWithoutABlocker() {
        Permanent flyer = addCreatureReady(player1, new Cloudskate());
        addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new Mossdog());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FogPatch(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(flyer.isBlockedWithoutBlockers()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    void cannotBeCastOutsideDeclareBlockersStep() {
        addCreatureReady(player1, new Mossdog());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player2, new FogPatch(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
