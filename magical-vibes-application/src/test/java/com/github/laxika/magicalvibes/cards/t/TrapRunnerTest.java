package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrapRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Makes an unblocked attacker blocked with no blocker")
    void makesUnblockedAttackerBlocked() {
        Permanent trapRunner = addCreatureReady(player1, new TrapRunner());
        Permanent attacker = addCreatureReady(player1, new SavannahLions());
        addCreatureReady(player2, new SavannahLions());
        declareAttackers(List.of(1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(trapRunner), null, attacker.getId());
        harness.passBothPriorities();

        assertThat(trapRunner.isTapped()).isTrue();
        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate outside the declare blockers step")
    void cannotActivateOutsideDeclareBlockers() {
        Permanent trapRunner = addCreatureReady(player1, new TrapRunner());
        Permanent attacker = addCreatureReady(player1, new SavannahLions());
        declareAttackers(List.of(1));

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(trapRunner), null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("declare blockers");
    }

    @Test
    @DisplayName("Cannot target an already blocked attacker")
    void cannotTargetBlockedAttacker() {
        Permanent trapRunner = addCreatureReady(player1, new TrapRunner());
        Permanent blockedAttacker = addCreatureReady(player1, new SavannahLions());
        addCreatureReady(player2, new SavannahLions());
        declareAttackers(List.of(1));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(trapRunner), null, blockedAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
