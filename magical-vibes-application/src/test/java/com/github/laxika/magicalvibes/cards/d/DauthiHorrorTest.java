package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DauthiHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Dauthi Horror can't be blocked by a white creature with shadow")
    void cannotBeBlockedByWhiteShadowCreature() {
        Permanent blockerPerm = setUpCombat(new SoltariFootSoldier());

        assertThatThrownBy(() -> declareBlock(blockerPerm))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dauthi Horror can be blocked by a non-white creature with shadow")
    void canBeBlockedByNonWhiteShadowCreature() {
        Permanent blockerPerm = setUpCombat(new DauthiGhoul());

        declareBlock(blockerPerm);

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Shadow stops a creature without shadow from blocking Dauthi Horror")
    void cannotBeBlockedByCreatureWithoutShadow() {
        Permanent blockerPerm = setUpCombat(new GrizzlyBears());

        assertThatThrownBy(() -> declareBlock(blockerPerm))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent setUpCombat(com.github.laxika.magicalvibes.model.Card blocker) {
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent atkPerm = new Permanent(new DauthiHorror());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        return blockerPerm;
    }

    private void declareBlock(Permanent blockerPerm) {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).size() - 1;
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }
}
