package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreadWarlockTest extends BaseCardTest {

    @Test
    @DisplayName("Dread Warlock cannot be blocked by a non-black creature")
    void cannotBeBlockedByNonBlackCreature() {
        Permanent warlock = attackingWarlock();
        gd.playerBattlefields.get(player1.getId()).add(warlock);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by black creatures");
    }

    @Test
    @DisplayName("Dread Warlock can be blocked by a black creature")
    void canBeBlockedByBlackCreature() {
        Permanent warlock = attackingWarlock();
        gd.playerBattlefields.get(player1.getId()).add(warlock);

        Permanent imp = new Permanent(new DuskImp());
        imp.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(imp);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== Helpers =====

    private Permanent attackingWarlock() {
        Permanent warlock = new Permanent(new DreadWarlock());
        warlock.setSummoningSick(false);
        warlock.setAttacking(true);
        return warlock;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
