package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HavengulVampire.class, GrizzlyBears.class, Shock.class})
class HavengulVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter after dealing combat damage to a player")
    void getsCounterOnCombatDamageToPlayer() {
        Permanent vampire = addCreatureReady(player1, new HavengulVampire());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities(); // resolve the combat damage trigger

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when another creature dies")
    void getsCounterWhenAnotherCreatureDies() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new HavengulVampire());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // death trigger resolves

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accumulates counters as multiple creatures die")
    void accumulatesCountersFromMultipleDeaths() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new HavengulVampire());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger for its own death")
    void doesNotTriggerForItsOwnDeath() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new HavengulVampire());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, vampire.getId());

        harness.assertInGraveyard(player1, "Havengul Vampire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A pending counter trigger cannot put counters on a dead source")
    void pendingCounterTriggerDoesNothingAfterSourceDies() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new HavengulVampire());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, vampire.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Havengul Vampire");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a blocker only grants the counter for its death")
    void combatDamageToCreatureDoesNotTriggerPlayerDamageAbility() {
        Permanent vampire = addCreatureReady(player1, new HavengulVampire());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HavengulVampire());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        addCreatureReady(player2, new HavengulVampire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Havengul Vampire");
        harness.assertNotOnBattlefield(player2, "Havengul Vampire");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocked combat triggers for each simultaneous death, but not player damage")
    void countsEachSimultaneousCombatDeathWithoutPlayerDamage() {
        addCreatureReady(player1, new HavengulVampire());
        addCreatureReady(player1, new HavengulVampire());
        Permanent observer = harness.addToBattlefieldAndReturn(player1, new HavengulVampire());
        addCreatureReady(player2, new HavengulVampire());
        addCreatureReady(player2, new HavengulVampire());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Havengul Vampire")).isEqualTo(1);
        assertThat(countPermanents(player2, "Havengul Vampire")).isZero();
        assertThat(observer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
