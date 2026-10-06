package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.b.BeaconOfDestruction;
import com.github.laxika.magicalvibes.cards.f.FurnaceWhelp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiteOfPassage.class, Arachnoid.class, FurnaceWhelp.class, BeaconOfDestruction.class})
class RiteOfPassageTest extends BaseCardTest {

    @Test
    @DisplayName("A damaged creature you control gets a +1/+1 counter")
    void damagedControlledCreatureGetsCounter() {
        harness.addToBattlefield(player1, new RiteOfPassage());
        Permanent arachnoid = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setHand(player2, List.of(new BeaconOfDestruction()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castAndResolveInstant(player2, 0, arachnoid.getId());
        resolveAllTriggers();

        assertThat(arachnoid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage to an opponent's creature does not trigger")
    void damagedOpponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new RiteOfPassage());
        Permanent arachnoid = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        harness.setHand(player2, List.of(new BeaconOfDestruction()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castAndResolveInstant(player2, 0, arachnoid.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(arachnoid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature that dies from the damage does not get a counter")
    void deadCreatureDoesNotGetCounter() {
        harness.addToBattlefield(player1, new RiteOfPassage());
        Permanent furnaceWhelp = harness.addToBattlefieldAndReturn(player1, new FurnaceWhelp());
        harness.setHand(player2, List.of(new BeaconOfDestruction()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castAndResolveInstant(player2, 0, furnaceWhelp.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Furnace Whelp");
    }

    @Test
    @DisplayName("Combat damage to a creature you control gets a +1/+1 counter")
    void combatDamageToControlledCreatureGetsCounter() {
        harness.addToBattlefield(player1, new RiteOfPassage());
        Permanent blocker = addCreatureReady(player1, new Arachnoid());
        Permanent attacker = addCreatureReady(player2, new FurnaceWhelp());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Rite of Passage adds one counter, regardless of the damage amount")
    void multipleCopiesEachAddOneCounter() {
        harness.addToBattlefield(player1, new RiteOfPassage());
        harness.addToBattlefield(player1, new RiteOfPassage());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setHand(player2, List.of(new BeaconOfDestruction()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to the controller does not trigger Rite of Passage")
    void playerDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new RiteOfPassage());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setHand(player2, List.of(new BeaconOfDestruction()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lethal combat damage still triggers but cannot save the creature")
    void lethalCombatDamageStillTriggers() {
        harness.addToBattlefield(player1, new RiteOfPassage());
        Permanent blocker = addCreatureReady(player1, new FurnaceWhelp());
        Permanent attacker = addCreatureReady(player2, new Arachnoid());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player2);

        harness.resolveCombatDamage();

        harness.assertNotOnBattlefield(player1, "Furnace Whelp");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
