package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.p.PrimevalShambler;
import com.github.laxika.magicalvibes.cards.r.RockBadger;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Deathgazer.class, GiantSpider.class, ScatheZombies.class, RockBadger.class, PrimevalShambler.class})
class DeathgazerTest extends BaseCardTest {

    @Test
    @DisplayName("When Deathgazer becomes blocked by a nonblack creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByNonblackSchedulesDestruction() {
        Permanent deathgazer = addCreatureReady(player1, new Deathgazer());
        deathgazer.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // A becomes-blocked trigger referencing the blocker is created
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Deathgazer")
                        && se.getTargetId().equals(spider.getId()));

        // Resolving it schedules the blocker for destruction at end of combat
        resolveAllTriggers();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

    @Test
    @DisplayName("Deathgazer creates one destruction trigger for each nonblack blocker")
    void eachNonblackBlockerCreatesItsOwnTrigger() {
        Permanent deathgazer = addCreatureReady(player1, new Deathgazer());
        deathgazer.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GiantSpider());
        Permanent secondBlocker = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack.stream()
                .filter(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(se -> se.getCard().getName().equals("Deathgazer")))
                .hasSize(2);

        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .extracting(DelayedPermanentAction::permanentId)
                .containsExactlyInAnyOrder(firstBlocker.getId(), secondBlocker.getId());
    }

    @Test
    @DisplayName("A nonblack blocker survives combat damage but is destroyed at end of combat")
    void nonblackBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent deathgazer = addCreatureReady(player1, new Deathgazer());
        deathgazer.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the trigger, then advance through end of combat
        resolveAllTriggers();
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When Deathgazer becomes blocked by a black creature, nothing is scheduled for destruction")
    void becomesBlockedByBlackSchedulesNothing() {
        Permanent deathgazer = addCreatureReady(player1, new Deathgazer());
        deathgazer.setAttacking(true);
        addCreatureReady(player2, new ScatheZombies());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Deathgazer"));
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("When Deathgazer blocks a nonblack creature, that attacker is scheduled for end-of-combat destruction")
    void blocksNonblackSchedulesDestruction() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Deathgazer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // The block trigger references the blocked attacker, not Deathgazer itself
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Deathgazer")
                        && se.getTargetId().equals(attacker.getId()));

        resolveAllTriggers();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("When Deathgazer blocks a black creature, nothing is scheduled for destruction")
    void blocksBlackSchedulesNothing() {
        Permanent attacker = addCreatureReady(player1, new ScatheZombies());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Deathgazer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Deathgazer"));
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
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
}
