package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.d.Deathlace;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.p.Purelace;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Abomination.class, BogWraith.class, Deathlace.class, GiantSpider.class, Purelace.class,
        SavannahLions.class, ScatheZombies.class})
class AbominationTest extends BaseCardTest {

    @Test
    @DisplayName("When Abomination becomes blocked by a green creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByGreenSchedulesDestruction() {
        Permanent abomination = addCreatureReady(player1, new Abomination());
        abomination.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Abomination")
                        && se.getTargetId().equals(spider.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

    @Test
    @DisplayName("When Abomination becomes blocked by a white creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByWhiteSchedulesDestruction() {
        Permanent abomination = addCreatureReady(player1, new Abomination());
        abomination.setAttacking(true);
        Permanent lions = addCreatureReady(player2, new SavannahLions());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(lions.getId()));
    }

    @Test
    @DisplayName("A green blocker survives combat damage but is destroyed at end of combat")
    void greenBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent abomination = addCreatureReady(player1, new Abomination());
        abomination.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When Abomination becomes blocked by a black creature, nothing is scheduled for destruction")
    void becomesBlockedByBlackSchedulesNothing() {
        Permanent abomination = addCreatureReady(player1, new Abomination());
        abomination.setAttacking(true);
        addCreatureReady(player2, new ScatheZombies());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("When Abomination blocks a green creature, that attacker is scheduled for end-of-combat destruction")
    void blocksGreenSchedulesDestruction() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Abomination());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Abomination")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("When Abomination blocks a black creature, nothing is scheduled for destruction")
    void blocksBlackSchedulesNothing() {
        Permanent attacker = addCreatureReady(player1, new ScatheZombies());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Abomination());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("A green creature that changes color after blocking is still destroyed at end of combat")
    void greenCreatureChangingColorAfterBlockingIsStillDestroyed() {
        Permanent abomination = addCreatureReady(player1, new Abomination());
        abomination.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        harness.setHand(player2, List.of(new Deathlace()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passPriority(player1);
        harness.castInstant(player2, 0, spider.getId());
        resolveAllTriggers();
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("A black creature that changes color after blocking does not become eligible for destruction")
    void blackCreatureChangingColorAfterBlockingIsNotDestroyed() {
        Permanent abomination = addCreatureReady(player1, new Abomination());
        abomination.setAttacking(true);
        Permanent wraith = addCreatureReady(player2, new BogWraith());

        harness.setHand(player2, List.of(new Purelace()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passPriority(player1);
        harness.castInstant(player2, 0, wraith.getId());
        resolveAllTriggers();
        resolveCombat();

        harness.assertOnBattlefield(player2, "Bog Wraith");
        harness.assertNotInGraveyard(player2, "Bog Wraith");
    }
}
