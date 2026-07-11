package com.github.laxika.magicalvibes.cards.f;

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

class FleetFootedMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Fleet-Footed Monk cannot be blocked by a creature with power 2 or greater")
    void cannotBeBlockedByPower2OrGreater() {
        Permanent monk = attackingMonk();
        gd.playerBattlefields.get(player1.getId()).add(monk);

        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Fleet-Footed Monk can be blocked by a creature with power 1 or less")
    void canBeBlockedByPower1OrLess() {
        Permanent monk = attackingMonk();
        gd.playerBattlefields.get(player1.getId()).add(monk);

        Permanent wizard = new Permanent(new FugitiveWizard()); // 1/1
        wizard.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wizard);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== Helpers =====

    private Permanent attackingMonk() {
        Permanent monk = new Permanent(new FleetFootedMonk());
        monk.setSummoningSick(false);
        monk.setAttacking(true);
        return monk;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
