package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.v.VampireAristocrat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SacredKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Sacred Knight can't be blocked by a black creature")
    void cannotBeBlockedByBlackCreature() {
        attackWithKnight();

        Permanent vampire = new Permanent(new VampireAristocrat()); // black
        vampire.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(vampire);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Sacred Knight can't be blocked by a red creature")
    void cannotBeBlockedByRedCreature() {
        attackWithKnight();

        Permanent giant = new Permanent(new HillGiant()); // red
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Sacred Knight can be blocked by a creature that is neither black nor red")
    void canBeBlockedByNonBlackNonRedCreature() {
        attackWithKnight();

        Permanent bears = new Permanent(new GrizzlyBears()); // green
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== Helpers =====

    private void attackWithKnight() {
        Permanent knight = new Permanent(new SacredKnight());
        knight.setSummoningSick(false);
        knight.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(knight);
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
