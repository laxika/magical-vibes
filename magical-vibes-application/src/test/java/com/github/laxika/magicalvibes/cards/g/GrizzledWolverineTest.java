package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrizzledWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 when blocked during the declare blockers step")
    void pumpsWhenBlocked() {
        Permanent wolverine = addCreatureReady(player1, new GrizzledWolverine());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        setupBlockedWolverine(wolverine, blocker);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(wolverine), null, null);
        harness.passBothPriorities();

        assertThat(wolverine.getPowerModifier()).isEqualTo(2);
        assertThat(wolverine.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent wolverine = addCreatureReady(player1, new GrizzledWolverine());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        setupBlockedWolverine(wolverine, blocker);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(wolverine), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolverine.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate when no creature is blocking it")
    void cannotActivateWhenUnblocked() {
        Permanent wolverine = addCreatureReady(player1, new GrizzledWolverine());
        wolverine.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(wolverine), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking this creature");
    }

    @Test
    @DisplayName("Cannot activate outside the declare blockers step")
    void cannotActivateOutsideDeclareBlockers() {
        Permanent wolverine = addCreatureReady(player1, new GrizzledWolverine());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        setupBlockedWolverine(wolverine, blocker);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(wolverine), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("declare blockers");
    }

    @Test
    @DisplayName("Cannot activate more than once each turn")
    void onlyOncePerTurn() {
        Permanent wolverine = addCreatureReady(player1, new GrizzledWolverine());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        setupBlockedWolverine(wolverine, blocker);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, battlefieldIndex(wolverine), null, null);
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(wolverine), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wolverine.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate again on a later turn")
    void canActivateAgainOnLaterTurn() {
        Permanent wolverine = addCreatureReady(player1, new GrizzledWolverine());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        setupBlockedWolverine(wolverine, blocker);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(wolverine), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        setupBlockedWolverine(wolverine, blocker);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(wolverine), null, null);
        harness.passBothPriorities();

        assertThat(wolverine.getPowerModifier()).isEqualTo(2);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void setupBlockedWolverine(Permanent wolverine, Permanent blocker) {
        wolverine.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(battlefieldIndex(wolverine));
        blocker.addBlockingTargetId(wolverine.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
