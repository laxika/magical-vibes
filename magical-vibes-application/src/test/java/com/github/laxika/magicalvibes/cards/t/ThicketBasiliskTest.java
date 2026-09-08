package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThicketBasilisk.class, GiantSpider.class, WallOfAir.class})
class ThicketBasiliskTest extends BaseCardTest {
    @Test
    @DisplayName("When Thicket Basilisk becomes blocked by a non-Wall creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByNonWallSchedulesDestruction() {
        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        basilisk.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Thicket Basilisk")
                        && se.getTargetId().equals(spider.getId()));

        resolveAllTriggers();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

    @Test
    @DisplayName("A non-Wall blocker survives combat damage but is destroyed at end of combat")
    void nonWallBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When Thicket Basilisk becomes blocked by a Wall creature, its ability does not trigger")
    void becomesBlockedByWallDoesNotTrigger() {
        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new WallOfAir());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Thicket Basilisk"));
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }
    @Test
    @DisplayName("When Thicket Basilisk blocks a non-Wall creature, that attacker is scheduled for end-of-combat destruction")
    void blocksNonWallSchedulesDestruction() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ThicketBasilisk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Thicket Basilisk")
                        && se.getTargetId().equals(attacker.getId()));

        resolveAllTriggers();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("When Thicket Basilisk blocks a Wall, its ability does not trigger")
    void blocksWallDoesNotTrigger() {
        Permanent wall = addCreatureReady(player1, new GiantSpider());
        TestCards.mutableCard(wall).setSubtypes(List.of(CardSubtype.WALL));
        wall.setAttacking(true);
        addCreatureReady(player2, new ThicketBasilisk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Thicket Basilisk"));
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("Thicket Basilisk creates one destruction trigger for each non-Wall blocker")
    void eachNonWallBlockerCreatesItsOwnTrigger() {
        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        basilisk.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GiantSpider());
        Permanent secondBlocker = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack.stream()
                .filter(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(se -> se.getCard().getName().equals("Thicket Basilisk")))
                .hasSize(2);

        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .extracting(DelayedPermanentAction::permanentId)
                .containsExactlyInAnyOrder(firstBlocker.getId(), secondBlocker.getId());
    }

    @Test
    @DisplayName("A non-Wall creature that becomes a Wall after blocking is still destroyed at end of combat")
    void nonWallConditionIsNotRecheckedAfterTriggering() {
        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        basilisk.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        TestCards.mutableCard(blocker).setSubtypes(List.of(CardSubtype.WALL));

        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("A non-Wall attacker blocked by Thicket Basilisk survives combat damage but is destroyed at end of combat")
    void nonWallAttackerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ThicketBasilisk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Giant Spider");
    }
}
