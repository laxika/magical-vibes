package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.v.VampireAristocrat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathgazerTest extends BaseCardTest {

    // ===== Deathgazer becomes blocked =====

    @Test
    @DisplayName("When Deathgazer becomes blocked by a nonblack creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByNonblackSchedulesDestruction() {
        Permanent deathgazer = addReadyDeathgazer(player1);
        deathgazer.setAttacking(true);
        Permanent spider = addReadySpider(player2); // green, 2/4

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // A becomes-blocked trigger referencing the blocker is created
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Deathgazer")
                        && se.getTargetId().equals(spider.getId()));

        // Resolving it schedules the blocker for destruction at end of combat
        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

    @Test
    @DisplayName("A nonblack blocker survives combat damage but is destroyed at end of combat")
    void nonblackBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent deathgazer = addReadyDeathgazer(player1);
        deathgazer.setAttacking(true);
        addReadySpider(player2); // 2/4 survives Deathgazer's 2 damage

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the trigger, then advance through end of combat
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("When Deathgazer becomes blocked by a black creature, nothing is scheduled for destruction")
    void becomesBlockedByBlackSchedulesNothing() {
        Permanent deathgazer = addReadyDeathgazer(player1);
        deathgazer.setAttacking(true);
        addReadyVampire(player2); // black

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // The trigger still fires but the nonblack filter fails at resolution
        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    // ===== Deathgazer blocks =====

    @Test
    @DisplayName("When Deathgazer blocks a nonblack creature, that attacker is scheduled for end-of-combat destruction")
    void blocksNonblackSchedulesDestruction() {
        Permanent attacker = addReadySpider(player1); // green, 2/4
        attacker.setAttacking(true);
        addReadyDeathgazer(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // The block trigger references the blocked attacker, not Deathgazer itself
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Deathgazer")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("When Deathgazer blocks a black creature, nothing is scheduled for destruction")
    void blocksBlackSchedulesNothing() {
        Permanent attacker = addReadyVampire(player1); // black
        attacker.setAttacking(true);
        addReadyDeathgazer(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyDeathgazer(Player player) {
        Permanent perm = new Permanent(new Deathgazer());
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

    private Permanent addReadyVampire(Player player) {
        Permanent perm = new Permanent(new VampireAristocrat());
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
