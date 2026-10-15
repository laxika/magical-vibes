package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DrownerOfSecrets;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YixlidJailer;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Vigor.class, GrizzlyBears.class, Shock.class, DrownerOfSecrets.class, YixlidJailer.class})
class VigorTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage to another creature you control is prevented and replaced with +1/+1 counters")
    void preventsNoncombatDamageToOtherCreature() {
        harness.addToBattlefield(player2, new Vigor());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        // Shock's 2 damage prevented; Bears survives with two +1/+1 counters.
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Vigor does not protect itself — its own damage is not prevented and adds no counters")
    void vigorDoesNotProtectItself() {
        harness.addToBattlefield(player2, new Vigor());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID vigorId = harness.getPermanentId(player2, "Vigor");
        harness.castAndResolveInstant(player1, 0, vigorId);

        Permanent vigor = findPermanent(player2, "Vigor");
        // "Another creature" — Vigor's own damage lands as marked damage, no +1/+1 counters.
        assertThat(vigor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vigor.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to another creature you control is prevented and replaced with +1/+1 counters")
    void preventsCombatDamageToOtherCreature() {
        harness.addToBattlefield(player2, new Vigor());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Blocker takes no combat damage — it survives with two +1/+1 counters.
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Vigor is put into the graveyard, a triggered ability shuffles it into its owner's library")
    void diesThenTriggerShufflesIntoLibrary() {
        harness.setLibrary(player2, new java.util.ArrayList<>());
        Permanent vigor = harness.addToBattlefieldAndReturn(player2, new Vigor());
        vigor.setMarkedDamage(6);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Vigor");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Vigor");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Vigor"));
    }

    @Test
    void doesNotProtectOpponentCreature() {
        harness.addToBattlefield(player2, new Vigor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotProtectController() {
        harness.addToBattlefield(player2, new Vigor());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void multipleVigorsDoNotMultiplyCounters() {
        harness.addToBattlefield(player2, new Vigor());
        harness.addToBattlefield(player2, new Vigor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void anotherVigorProtectsVigor() {
        Permanent vigor = harness.addToBattlefieldAndReturn(player2, new Vigor());
        harness.addToBattlefield(player2, new Vigor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, vigor.getId());

        assertThat(vigor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(vigor.getMarkedDamage()).isZero();
    }

    @Test
    void unpreventableDamageDoesNotAddCounters() {
        harness.addToBattlefield(player2, new Vigor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setDamageCantBePreventedOrRedirectedThisTurn(true);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void abilityLossOnBattlefieldDisablesPrevention() {
        Permanent vigor = harness.addToBattlefieldAndReturn(player2, new Vigor());
        vigor.setLosesAllAbilitiesUntilEndOfTurn(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void abilityLossOnBattlefieldDoesNotDisableGraveyardTrigger() {
        harness.setLibrary(player2, List.of());
        Permanent vigor = harness.addToBattlefieldAndReturn(player2, new Vigor());
        vigor.setLosesAllAbilitiesUntilEndOfTurn(true);
        vigor.setMarkedDamage(6);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Vigor");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Vigor");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(vigor.getCard());
    }

    @Test
    void millingTriggersShuffle() {
        Vigor vigor = new Vigor();
        harness.setLibrary(player2, List.of(vigor));
        addCreatureReady(player1, new DrownerOfSecrets());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vigor");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Vigor");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(vigor);
    }

    @Test
    void graveyardAbilityLossPreventsShuffleTrigger() {
        Vigor vigor = new Vigor();
        harness.setLibrary(player2, List.of(vigor));
        addCreatureReady(player1, new DrownerOfSecrets());
        harness.addToBattlefield(player1, new YixlidJailer());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vigor");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
