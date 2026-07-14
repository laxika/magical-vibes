package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DestroyPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThicketBasiliskTest extends BaseCardTest {

    // ===== Thicket Basilisk becomes blocked =====

    @Test
    @DisplayName("When Thicket Basilisk becomes blocked by a non-Wall creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByNonWallSchedulesDestruction() {
        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);
        Permanent spider = addReadySpider(player2); // non-Wall

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Thicket Basilisk")
                        && se.getTargetId().equals(spider.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DestroyPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

    @Test
    @DisplayName("A non-Wall blocker survives combat damage but is destroyed at end of combat")
    void nonWallBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);
        addReadySpider(player2); // 2/4 survives Basilisk's 2 damage

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("When Thicket Basilisk becomes blocked by a Wall creature, nothing is scheduled for destruction")
    void becomesBlockedByWallSchedulesNothing() {
        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);
        addReadyWall(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DestroyPermanentAtEndOfCombat.class)).isFalse();
    }

    // ===== Thicket Basilisk blocks =====

    @Test
    @DisplayName("When Thicket Basilisk blocks a non-Wall creature, that attacker is scheduled for end-of-combat destruction")
    void blocksNonWallSchedulesDestruction() {
        Permanent attacker = addReadySpider(player1); // non-Wall
        attacker.setAttacking(true);
        addReadyBasilisk(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Thicket Basilisk")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DestroyPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    // ===== Helpers =====

    private Permanent addReadyBasilisk(Player player) {
        Permanent perm = new Permanent(new ThicketBasilisk());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyWall(Player player) {
        Permanent perm = new Permanent(new WallOfWood());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
