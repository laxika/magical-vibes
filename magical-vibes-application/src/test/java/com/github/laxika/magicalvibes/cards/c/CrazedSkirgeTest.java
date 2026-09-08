package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrazedSkirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Crazed Skirge attack immediately")
    void hasteLetsItAttackImmediately() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new CrazedSkirge());

        declareAttackers(player1, List.of(0));

        assertThat(skirge.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Crazed Skirge")
    void flyingPreventsGroundCreatureFromBlocking() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new CrazedSkirge());
        skirge.setSummoningSick(false);
        skirge.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
