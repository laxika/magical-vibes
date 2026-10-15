package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
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

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);
        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .anyMatch(a -> a.affectedPermanentId().equals(spider.getId()));
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

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, this::resolveAllTriggers);

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
        assertThat(gd.hasDelayedAction(DelayedEndOfCombatTrigger.class)).isFalse();
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

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);
        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .anyMatch(a -> a.affectedPermanentId().equals(attacker.getId()));
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
        assertThat(gd.hasDelayedAction(DelayedEndOfCombatTrigger.class)).isFalse();
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

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .extracting(DelayedEndOfCombatTrigger::affectedPermanentId)
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

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .anyMatch(a -> a.affectedPermanentId().equals(blocker.getId()));
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

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, this::resolveAllTriggers);

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Destruction waits for the end-of-combat trigger to resolve and allows regeneration")
    void delayedDestructionCanBeRegenerated() {
        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        basilisk.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Thicket Basilisk"));

        blocker.setRegenerationShield(1);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, this::resolveAllTriggers);

        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
        assertThat(blocker.getRegenerationShield()).isZero();
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The non-Wall blocker is destroyed even if Thicket Basilisk dies in combat")
    void delayedDestructionSurvivesSourceDeath() {
        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        basilisk.setAttacking(true);
        basilisk.setMarkedDamage(2);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertInGraveyard(player1, "Thicket Basilisk");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.withAutoStop(TurnStep.END_OF_COMBAT, this::resolveAllTriggers);

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Only the non-Wall blocker gets a destruction trigger among mixed blockers")
    void mixedBlockersOnlyScheduleNonWallDestruction() {
        Permanent basilisk = addCreatureReady(player1, new ThicketBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new WallOfAir());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .extracting(DelayedEndOfCombatTrigger::affectedPermanentId)
                .containsExactly(spider.getId());
    }
}
