package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.cards.a.AnimateWall;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThicketBasilisk.class, GiantSpider.class, WallOfWood.class, AnimateWall.class})
class ThicketBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("When Thicket Basilisk becomes blocked by a non-Wall creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByNonWallSchedulesDestruction() {
        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Thicket Basilisk")
                        && se.getTargetId().equals(spider.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

    @Test
    @DisplayName("A non-Wall blocker survives combat damage but is destroyed at end of combat")
    void nonWallBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        addCreatureReady(player2, new GiantSpider());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When Thicket Basilisk becomes blocked by a Wall creature, nothing is scheduled for destruction")
    void becomesBlockedByWallSchedulesNothing() {
        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        addCreatureReady(player2, new WallOfWood());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("When Thicket Basilisk blocks a non-Wall creature, that attacker is scheduled for end-of-combat destruction")
    void blocksNonWallSchedulesDestruction() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player2, new ThicketBasilisk());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Thicket Basilisk")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("When Thicket Basilisk blocks a Wall creature, its ability does not trigger")
    void blocksWallDoesNotTrigger() {
        Permanent wall = addCreatureReady(player1, new WallOfWood());
        Permanent animateWall = harness.addToBattlefieldAndReturn(player1, new AnimateWall());
        animateWall.setAttachedTo(wall.getId());
        addCreatureReady(player2, new ThicketBasilisk());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard() instanceof ThicketBasilisk);
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("When Thicket Basilisk becomes blocked by multiple creatures, only non-Wall blockers are scheduled for destruction")
    void becomesBlockedByMultipleCreaturesSchedulesOnlyNonWallBlockers() {
        addCreatureReady(player1, new ThicketBasilisk());
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        Permanent wall = addCreatureReady(player2, new WallOfWood());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack)
                .filteredOn(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard() instanceof ThicketBasilisk)
                .extracting(se -> se.getTargetId())
                .containsExactly(spider.getId());

        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()))
                .noneMatch(a -> a.permanentId().equals(wall.getId()));
    }
}
