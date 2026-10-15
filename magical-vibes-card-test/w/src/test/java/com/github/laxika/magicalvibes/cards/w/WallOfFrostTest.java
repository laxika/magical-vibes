package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfFrost.class, RuneclawBear.class})
class WallOfFrostTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring Wall of Frost as blocker pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent wallPerm = addReadyWall(player2);
        Permanent atkPerm = addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(atkPerm.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(wallPerm.getId());
    }

    @Test
    @DisplayName("Block trigger is non-targeting (cannot fizzle)")
    void blockTriggerIsNonTargeting() {
        addReadyWall(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving block trigger sets skipUntapCount on the blocked creature")
    void resolvingSetsSkipUntapCount() {
        addReadyWall(player2);
        Permanent atkPerm = addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(atkPerm.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocked creature remains on the battlefield after trigger resolves")
    void blockedCreatureRemainsOnBattlefield() {
        addReadyWall(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Wall of Frost remains on the battlefield after trigger resolves")
    void wallRemainsOnBattlefield() {
        addReadyWall(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Frost");
    }

    @Test
    @DisplayName("Trigger does nothing if attacker is removed before resolution")
    void triggerDoesNothingIfAttackerRemoved() {
        addReadyWall(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        // Remove attacker before trigger resolves
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Stack should be empty, no crash
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Block trigger generates appropriate game log entry")
    void blockTriggerGeneratesLog() {
        addReadyWall(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Wall of Frost") && log.contains("block") && log.contains("trigger"));
    }

    @Test
    @DisplayName("Resolving trigger logs that creature won't untap")
    void resolvingLogsSkipUntap() {
        addReadyWall(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Runeclaw Bear") && log.contains("untap"));
    }

    @Test
    void skipsOnlyTheNextControllersUntapStep() {
        addReadyWall(player2);
        Permanent attacker = addReadyAttacker(player1);
        attacker.tap();
        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void untappedCreatureConsumesRestrictionAtNextUntapStep() {
        addReadyWall(player2);
        Permanent attacker = addReadyAttacker(player1);
        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        harness.performUntapStep(player1);
        attacker.tap();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void triggerResolvesAfterWallLeavesBattlefield() {
        addReadyWall(player2);
        Permanent attacker = addReadyAttacker(player1);
        attacker.tap();
        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void restrictionFollowsCreatureWhenControllerChanges() {
        addReadyWall(player2);
        Permanent attacker = addReadyAttacker(player1);
        attacker.tap();
        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.performUntapStep(player1);
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void twoWallsRestrictTheSameUntapStep() {
        addReadyWall(player2);
        addReadyWall(player2);
        Permanent attacker = addReadyAttacker(player1);
        attacker.tap();
        declareBlockers(List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    private Permanent addReadyWall(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new WallOfFrost());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new RuneclawBear());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        return perm;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, assignments);
    }
}
