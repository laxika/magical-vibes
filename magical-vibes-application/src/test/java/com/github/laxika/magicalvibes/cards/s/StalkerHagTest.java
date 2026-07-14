package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

class StalkerHagTest extends BaseCardTest {

    private void attemptBlock() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent atkPerm = new Permanent(new StalkerHag());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        this.blockerPerm = blockerPerm;
        this.blockAttempt = () -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }

    private Permanent blockerPerm;
    private Runnable blockAttempt;

    @Test
    @DisplayName("Cannot be blocked when defending player controls a Swamp")
    void cannotBeBlockedThroughSwamp() {
        harness.addToBattlefield(player2, new Swamp());
        attemptBlock();

        assertThatThrownBy(blockAttempt::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Cannot be blocked when defending player controls a Forest")
    void cannotBeBlockedThroughForest() {
        harness.addToBattlefield(player2, new Forest());
        attemptBlock();

        assertThatThrownBy(blockAttempt::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked when defending player controls neither a Swamp nor a Forest")
    void canBeBlockedWithoutSwampOrForest() {
        attemptBlock();

        blockAttempt.run();

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
