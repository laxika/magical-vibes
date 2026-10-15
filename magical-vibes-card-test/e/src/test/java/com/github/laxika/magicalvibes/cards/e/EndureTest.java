package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Endure.class, GrizzlyBears.class, Shock.class, ChandraNalaar.class})
class EndureTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Endure prevents all damage to the controller")
    void preventsDamageToController() {
        harness.setLife(player2, 20);
        harness.castFromHand(player2, new Endure(), "{3}{W}{W}");

        harness.passBothPriorities();

        // Now burn the protected player.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Resolving Endure prevents damage to a permanent the controller controls")
    void preventsDamageToControlledPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.castFromHand(player2, new Endure(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        // Grizzly Bears (2/2) takes 2 damage from Shock, but it is prevented.
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving Endure prevents damage to a noncreature permanent the controller controls")
    void preventsDamageToControlledPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        harness.castFromHand(player2, new Endure(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("Endure does not protect the opponent")
    void doesNotProtectOpponent() {
        harness.setLife(player1, 20);
        harness.castFromHand(player2, new Endure(), "{3}{W}{W}");

        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void preventsRepeatedDamageAndProtectsPermanentsEnteringLater() {
        harness.castFromHand(player2, new Endure(), "{3}{W}{W}");
        harness.passBothPriorities();
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        for (int i = 0; i < 2; i++) {
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castAndResolveInstant(player1, 0, creature.getId());
        }

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotProtectOpposingCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player2, new Endure(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void preventsCombatDamageToController() {
        harness.setLife(player2, 20);
        harness.castFromHand(player2, new Endure(), "{3}{W}{W}");
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);

        harness.assertLife(player2, 20);
    }

    @Test
    void preventionExpiresAfterTheTurn() {
        harness.setLife(player2, 20);
        harness.castFromHand(player2, new Endure(), "{3}{W}{W}");
        harness.passBothPriorities();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void preventsCombatDamageToBlockerWithoutPreventingItsDamage() {
        harness.castFromHand(player2, new Endure(), "{3}{W}{W}");
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
