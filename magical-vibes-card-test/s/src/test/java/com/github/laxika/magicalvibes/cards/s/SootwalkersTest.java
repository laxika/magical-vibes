package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.w.WatchwingScarecrow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sootwalkers.class, BallynockCohort.class, DevotedDruid.class, SafeholdElite.class, WatchwingScarecrow.class})
class SootwalkersTest extends BaseCardTest {

    @Test
    @DisplayName("Sootwalkers can't be blocked by a white creature")
    void cannotBeBlockedByWhiteCreature() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());
        blockerPerm.setSummoningSick(false);

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new Sootwalkers());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sootwalkers can be blocked by a non-white creature")
    void canBeBlockedByNonWhiteCreature() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new DevotedDruid());
        blockerPerm.setSummoningSick(false);

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new Sootwalkers());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sootwalkers can't be blocked by a multicolored white creature")
    void cannotBeBlockedByMulticoloredWhiteCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Sootwalkers());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sootwalkers can be blocked by a colorless creature")
    void canBeBlockedByColorlessCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WatchwingScarecrow());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Sootwalkers());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
