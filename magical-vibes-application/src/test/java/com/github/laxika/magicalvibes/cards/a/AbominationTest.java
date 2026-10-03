package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.d.Deathlace;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.p.Purelace;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Abomination.class, BogWraith.class, Deathlace.class, GiantSpider.class, Purelace.class,
        SavannahLions.class, ScatheZombies.class, Unsummon.class})
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
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertInGraveyard(player2, "Giant Spider");
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
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertInGraveyard(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("When Abomination becomes blocked by multiple creatures, each green or white blocker is scheduled for destruction")
    void becomesBlockedByMultipleColoredCreaturesSchedulesEachMatchingBlocker() {
        Permanent abomination = addCreatureReady(player1, new Abomination());
        abomination.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        Permanent lions = addCreatureReady(player2, new SavannahLions());
        addCreatureReady(player2, new ScatheZombies());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        resolveAllTriggers();

        harness.passUntil(TurnStep.COMBAT_DAMAGE);
        harness.handleCombatDamageAssigned(player1, 0, java.util.Map.of(spider.getId(), 2, lions.getId(), 0,
                gd.playerBattlefields.get(player2.getId()).get(2).getId(), 0));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Savannah Lions");
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

        resolveAllTriggers();
        resolveCombat();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

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
        assertThat(gd.stack).isEmpty();
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
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertInGraveyard(player1, "Giant Spider");
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
        assertThat(gd.stack).isEmpty();
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
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

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
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Bog Wraith");
        harness.assertNotInGraveyard(player2, "Bog Wraith");
    }

    @Test
    @DisplayName("End-of-combat destruction uses the stack and allows the creature to be saved in response")
    void canRespondToEndOfCombatDestruction() {
        Permanent abomination = addCreatureReady(player1, new Abomination());
        abomination.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        harness.setHand(player2, List.of(new Unsummon()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spider.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Removing Abomination before its block trigger resolves does not prevent delayed destruction")
    void removingSourceDoesNotPreventDestruction() {
        Permanent abomination = addCreatureReady(player1, new Abomination());
        abomination.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, abomination.getId());
        resolveAllTriggers();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInHand(player1, "Abomination");
        harness.assertInGraveyard(player2, "Giant Spider");
    }
}
