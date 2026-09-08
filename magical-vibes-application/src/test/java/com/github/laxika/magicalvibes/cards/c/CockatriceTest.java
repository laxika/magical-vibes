package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
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
        ImprisonedInTheMoon.class})
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
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
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

        harness.passBothPriorities();
        harness.passBothPriorities();

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
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
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
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
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

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()))
                .noneMatch(a -> a.permanentId().equals(wall.getId()));
    }

    @Test
    void blocksWallDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        Permanent amoeboid = addCreatureReady(player1, new AmoeboidChangeling());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(amoeboid), 0, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(GameQueryService.permanentHasSubtype(attacker, CardSubtype.WALL)).isTrue();

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
        assertThat(GameQueryService.permanentHasSubtype(spider, CardSubtype.WALL)).isTrue();

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

    @Test
    void nonWallBlockerBecomingNonCreatureRemainsScheduled() {
        Permanent cockatrice = addCreatureReady(player1, new Cockatrice());
        cockatrice.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(spider.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        assertThat(gqs.isCreature(gd, spider)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

}
