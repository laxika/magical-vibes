package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.cards.p.PrimevalShambler;
import com.github.laxika.magicalvibes.cards.r.RockBadger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Deathgazer.class, RockBadger.class, PrimevalShambler.class})
class DeathgazerTest extends BaseCardTest {

    // ===== Deathgazer becomes blocked =====

    @Test
    @DisplayName("When Deathgazer becomes blocked by a nonblack creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByNonblackSchedulesDestruction() {
        Permanent deathgazer = addCreatureReady(player1, new Deathgazer());
        deathgazer.setAttacking(true);
        Permanent rockBadger = addCreatureReady(player2, new RockBadger()); // red, 3/3

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // A becomes-blocked trigger referencing the blocker is created
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Deathgazer")
                        && se.getTargetId().equals(rockBadger.getId()));

        // Resolving it schedules the blocker for destruction at end of combat
        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(rockBadger.getId()));
    }

    @Test
    @DisplayName("A nonblack blocker survives combat damage but is destroyed at end of combat")
    void nonblackBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent deathgazer = addCreatureReady(player1, new Deathgazer());
        deathgazer.setAttacking(true);
        addCreatureReady(player2, new RockBadger()); // 3/3 survives Deathgazer's 2 damage

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the trigger, then advance through end of combat
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rock Badger");
        harness.assertInGraveyard(player2, "Rock Badger");
    }

    @Test
    @DisplayName("A nonblack creature blocked by Deathgazer is destroyed at end of combat")
    void nonblackAttackerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent rockBadger = addCreatureReady(player1, new RockBadger());
        rockBadger.setAttacking(true);
        addCreatureReady(player2, new Deathgazer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rock Badger");
        harness.assertInGraveyard(player1, "Rock Badger");
    }

    @Test
    @DisplayName("When Deathgazer becomes blocked by a black creature, nothing is scheduled for destruction")
    void becomesBlockedByBlackSchedulesNothing() {
        Permanent deathgazer = addCreatureReady(player1, new Deathgazer());
        deathgazer.setAttacking(true);
        addCreatureReady(player2, new PrimevalShambler());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Deathgazer"));
        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    // ===== Deathgazer blocks =====

    @Test
    @DisplayName("When Deathgazer blocks a nonblack creature, that attacker is scheduled for end-of-combat destruction")
    void blocksNonblackSchedulesDestruction() {
        Permanent attacker = addCreatureReady(player1, new RockBadger()); // red, 3/3
        attacker.setAttacking(true);
        addCreatureReady(player2, new Deathgazer());

        prepareDeclareBlockers();
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
    @DisplayName("Becoming blocked by multiple nonblack creatures creates one destruction for each blocker")
    void becomesBlockedByMultipleNonblackCreaturesSchedulesEachBlocker() {
        Permanent deathgazer = addCreatureReady(player1, new Deathgazer());
        deathgazer.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new RockBadger());
        Permanent secondBlocker = addCreatureReady(player2, new RockBadger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.stack).filteredOn(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Deathgazer"))
                .hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .extracting(DelayedPermanentAction::permanentId)
                .containsExactlyInAnyOrder(firstBlocker.getId(), secondBlocker.getId());
    }

    @Test
    @DisplayName("When Deathgazer blocks a black creature, nothing is scheduled for destruction")
    void blocksBlackSchedulesNothing() {
        Permanent attacker = addCreatureReady(player1, new PrimevalShambler()); // black
        attacker.setAttacking(true);
        addCreatureReady(player2, new Deathgazer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Deathgazer"));
        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

}
