package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

class SteelLeafChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Steel Leaf Champion cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPower2OrLess() {
        Permanent champion = attackingChampion();
        gd.playerBattlefields.get(player1.getId()).add(champion);

        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Steel Leaf Champion can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPower3OrGreater() {
        Permanent champion = attackingChampion();
        gd.playerBattlefields.get(player1.getId()).add(champion);

        Permanent giant = new Permanent(new HillGiant()); // 3/3
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== Helpers =====

    private Permanent attackingChampion() {
        Permanent champion = new Permanent(new SteelLeafChampion());
        champion.setSummoningSick(false);
        champion.setAttacking(true);
        return champion;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
