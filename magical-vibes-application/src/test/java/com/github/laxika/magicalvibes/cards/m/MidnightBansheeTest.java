package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.b.BoonReflection;
import com.github.laxika.magicalvibes.cards.c.Cinderbones;
import com.github.laxika.magicalvibes.cards.s.SpiteflameWitch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightBanshee.class, BallynockCohort.class, BoonReflection.class, Cinderbones.class, SpiteflameWitch.class})
class MidnightBansheeTest extends BaseCardTest {

    @Test
    @DisplayName("Your upkeep puts a -1/-1 counter on each nonblack creature (all players)")
    void upkeepCountersNonblackCreatures() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BallynockCohort());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Black creatures (including the Banshee) get no counter")
    void blackCreaturesUnaffected() {
        Permanent banshee = harness.addToBattlefieldAndReturn(player1, new MidnightBanshee());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new Cinderbones());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(banshee.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(blackCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Noncreature permanents get no counter")
    void noncreaturesUnaffected() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new BoonReflection());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(enchantment.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature that is both black and red is unaffected by the upkeep ability")
    void multicoloredBlackCreatureUnaffected() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SpiteflameWitch());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Spiteflame Witch");
    }

    @Test
    @DisplayName("The upkeep ability affects creatures entering after it triggers")
    void creaturesAreDeterminedAtResolution() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Two Banshees each put a counter on nonblack creatures")
    void multipleUpkeepTriggersAccumulateCounters() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        harness.addToBattlefield(player1, new MidnightBanshee());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Ballynock Cohort");
        harness.assertNotOnBattlefield(player2, "Ballynock Cohort");
    }

    @Test
    @DisplayName("Wither deals combat damage as counters even to a black creature")
    void witherAffectsBlackCreaturesInCombat() {
        Permanent attacker = addCreatureReady(player1, new MidnightBanshee());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MidnightBanshee());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Midnight Banshee");
        harness.assertInGraveyard(player2, "Midnight Banshee");
    }

    @Test
    @DisplayName("Wither deals normal damage to a player")
    void witherDealsNormalDamageToPlayer() {
        Permanent attacker = addCreatureReady(player1, new MidnightBanshee());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 15);
    }
}
