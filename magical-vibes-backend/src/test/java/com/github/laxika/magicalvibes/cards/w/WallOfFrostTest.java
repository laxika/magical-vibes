package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapOnTargetEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WallOfFrostTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Wall of Frost has SkipNextUntapOnTargetEffect in ON_BLOCK slot")
    void hasCorrectProperties() {
        WallOfFrost card = new WallOfFrost();

        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BLOCK).getFirst())
                .isInstanceOf(SkipNextUntapOnTargetEffect.class);
    }

    // ===== Block trigger pushes onto stack =====

    @Test
    @DisplayName("Declaring Wall of Frost as blocker pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent wallPerm = addReadyWall(player2);
        Permanent atkPerm = addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Wall of Frost");
        assertThat(entry.getTargetPermanentId()).isEqualTo(atkPerm.getId());
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

    // ===== Block trigger resolution =====

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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Wall of Frost remains on the battlefield after trigger resolves")
    void wallRemainsOnBattlefield() {
        addReadyWall(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wall of Frost"));
    }

    // ===== Attacker removed before resolution =====

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

    // ===== Game log =====

    @Test
    @DisplayName("Block trigger generates appropriate game log entry")
    void blockTriggerGeneratesLog() {
        addReadyWall(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Wall of Frost") && log.contains("block") && log.contains("trigger"));
    }

    @Test
    @DisplayName("Resolving trigger logs that creature won't untap")
    void resolvingLogsSkipUntap() {
        addReadyWall(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Grizzly Bears") && log.contains("untap"));
    }

    // ===== Helpers =====

    private Permanent addReadyWall(com.github.laxika.magicalvibes.model.Player player) {
        WallOfFrost card = new WallOfFrost();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyAttacker(com.github.laxika.magicalvibes.model.Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
        gs.declareBlockers(gd, player2, assignments);
    }
}
