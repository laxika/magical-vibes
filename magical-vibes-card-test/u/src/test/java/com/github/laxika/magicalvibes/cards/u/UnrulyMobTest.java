package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({UnrulyMob.class, GrizzlyBears.class, Shock.class})
class UnrulyMobTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when an ally creature dies")
    void getsCounterWhenAllyCreatureDies() {
        harness.addToBattlefield(player1, new UnrulyMob());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent unrulyMob = findPermanent(player1, "Unruly Mob");
        assertThat(unrulyMob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Kill ally creature with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Unruly Mob's +1/+1 counter trigger

        assertThat(unrulyMob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, unrulyMob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unrulyMob)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does NOT get a counter when an opponent's creature dies")
    void doesNotGetCounterWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new UnrulyMob());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent unrulyMob = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(unrulyMob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        // No trigger should have been added for Unruly Mob
        assertThat(unrulyMob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Accumulates multiple +1/+1 counters as multiple ally creatures die")
    void accumulatesCounters() {
        harness.addToBattlefield(player1, new UnrulyMob());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent unrulyMob = findPermanent(player1, "Unruly Mob");

        // Kill first ally creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Unruly Mob's trigger

        assertThat(unrulyMob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Add and kill second ally creature
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bears2Id = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bears2Id);
        harness.passBothPriorities(); // Resolve Unruly Mob's trigger

        assertThat(unrulyMob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, unrulyMob)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, unrulyMob)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for its own death")
    void doesNotTriggerForItsOwnDeath() {
        harness.addToBattlefield(player1, new UnrulyMob());
        UUID mobId = harness.getPermanentId(player1, "Unruly Mob");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, mobId);

        harness.assertInGraveyard(player1, "Unruly Mob");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers when another Unruly Mob dies, only after resolution")
    void triggersForAnotherMobAndWaitsForResolution() {
        Permanent survivingMob = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        Permanent dyingMob = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, dyingMob.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(survivingMob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(survivingMob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A pending counter trigger does not affect a different Unruly Mob")
    void pendingTriggerDoesNotAffectAnotherMobAfterSourceDies() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertNotOnBattlefield(player1, "Unruly Mob");
        Permanent otherMob = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());

        harness.passBothPriorities();

        assertThat(otherMob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers separately for each creature that dies simultaneously")
    void triggersSeparatelyForSimultaneousDeaths() {
        Permanent mob = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers for another creature dying simultaneously with it")
    void triggersWhenItDiesSimultaneouslyWithAnotherCreature() {
        Permanent mob = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        mob.setMarkedDamage(1);
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Unruly Mob");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
