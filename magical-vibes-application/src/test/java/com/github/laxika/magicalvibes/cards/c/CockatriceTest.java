package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cockatrice.class, GiantSpider.class, WallOfAir.class, AmoeboidChangeling.class,
        ImprisonedInTheMoon.class, Unsummon.class})
class CockatriceTest extends BaseCardTest {

    @Test
    @DisplayName("When Cockatrice becomes blocked by a non-Wall creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByNonWallSchedulesDestruction() {
        Permanent cockatrice = addCreatureReady(player1, new Cockatrice());
        cockatrice.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Cockatrice")
                        && se.getTargetId().equals(spider.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .anyMatch(a -> a.affectedPermanentId().equals(spider.getId()));
    }

    @Test
    @DisplayName("A non-Wall blocker survives combat damage but is destroyed at end of combat")
    void nonWallBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent cockatrice = addCreatureReady(player1, new Cockatrice());
        cockatrice.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.assertOnBattlefield(player2, "Giant Spider");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When Cockatrice becomes blocked by a Wall, its ability does not trigger")
    void becomesBlockedByWallDoesNotTrigger() {
        Permanent cockatrice = addCreatureReady(player1, new Cockatrice());
        cockatrice.setAttacking(true);
        addCreatureReady(player2, new WallOfAir());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard() instanceof Cockatrice);

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedEndOfCombatTrigger.class)).isFalse();
    }

    @Test
    @DisplayName("When Cockatrice blocks a non-Wall creature, that attacker is scheduled for end-of-combat destruction")
    void blocksNonWallSchedulesDestruction() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Cockatrice());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Cockatrice")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .anyMatch(a -> a.affectedPermanentId().equals(attacker.getId()));
    }

    @Test
    void becomesBlockedByMultipleCreaturesSchedulesOnlyNonWallBlockers() {
        Permanent cockatrice = addCreatureReady(player1, new Cockatrice());
        cockatrice.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        Permanent wall = addCreatureReady(player2, new WallOfAir());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack)
                .filteredOn(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard() instanceof Cockatrice)
                .extracting(se -> se.getTargetId())
                .containsExactly(spider.getId());

        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .anyMatch(a -> a.affectedPermanentId().equals(spider.getId()))
                .noneMatch(a -> a.affectedPermanentId().equals(wall.getId()));
    }

    @Test
    void blocksWallDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        Permanent amoeboid = addCreatureReady(player1, new AmoeboidChangeling());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(amoeboid), 0, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasEffectiveSubtype(gd, attacker, CardSubtype.WALL)).isTrue();

        attacker.setAttacking(true);
        addCreatureReady(player2, new Cockatrice());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard() instanceof Cockatrice);
    }

    @Test
    void nonWallBlockerBecomingWallRemainsScheduled() {
        Permanent cockatrice = addCreatureReady(player1, new Cockatrice());
        cockatrice.setAttacking(true);
        Permanent amoeboid = addCreatureReady(player1, new AmoeboidChangeling());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard() instanceof Cockatrice
                        && se.getTargetId().equals(spider.getId()));

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(amoeboid), 0, null, spider.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasEffectiveSubtype(gd, spider, CardSubtype.WALL)).isTrue();

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .anyMatch(a -> a.affectedPermanentId().equals(spider.getId()));
    }

    @Test
    void nonWallBlockerBecomingNonCreatureRemainsScheduled() {
        Permanent cockatrice = addCreatureReady(player1, new Cockatrice());
        cockatrice.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(spider.getId());
        assertThat(gqs.isCreature(gd, spider)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .anyMatch(a -> a.affectedPermanentId().equals(spider.getId()));
    }

    @Test
    void blockedAttackerDestroyedAtEndOfCombat() {
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        spider.setAttacking(true);
        addCreatureReady(player2, new Cockatrice());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.assertOnBattlefield(player1, "Giant Spider");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertOnBattlefield(player2, "Cockatrice");
    }

    @Test
    void destructionStillOccursWhenCockatriceLeavesBeforeInitialTriggerResolves() {
        Permanent cockatrice = addCreatureReady(player1, new Cockatrice());
        cockatrice.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castInstant(player1, 0, cockatrice.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Cockatrice");
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.assertOnBattlefield(player2, "Giant Spider");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    void delayedDestructionStillAffectsBlockerThatIsNoLongerACreature() {
        Permanent cockatrice = addCreatureReady(player1, new Cockatrice());
        cockatrice.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(spider.getId());
        assertThat(gqs.isCreature(gd, spider)).isFalse();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    void blockerCanBeReturnedInResponseToDelayedDestruction() {
        Permanent cockatrice = addCreatureReady(player1, new Cockatrice());
        cockatrice.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        harness.setHand(player2, List.of(new Unsummon()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.stack).anyMatch(se -> se.getCard() instanceof Cockatrice
                && spider.getId().equals(se.getTargetId()));

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, spider.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
    }
}
