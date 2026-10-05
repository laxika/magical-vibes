package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necroskitter.class, GrizzlyBears.class, Shock.class})
class NecroskitterTest extends BaseCardTest {

    @Test
    @DisplayName("Wither deals combat damage as -1/-1 counters")
    void witherDealsMinusOneMinusOneCounters() {
        Permanent attacker = addCreatureReady(player1, new Necroskitter());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Accepting returns the dying opponent creature to the battlefield under your control")
    void acceptingReturnsCreatureUnderControl() {
        harness.addToBattlefield(player1, new Necroskitter());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2 → 1/1
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, dyingId); // Shock resolves → Grizzly Bears dies with a -1/-1 counter
        harness.passBothPriorities(); // Necroskitter's MayEffect resolves from the stack → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // The card is now a permanent under player1's control, gone from player2's graveyard.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the dying creature in its owner's graveyard")
    void decliningLeavesCreatureInGraveyard() {
        harness.addToBattlefield(player1, new Necroskitter());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, dyingId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when the dying creature had no -1/-1 counter")
    void doesNotTriggerWithoutMinusOneCounter() {
        harness.addToBattlefield(player1, new Necroskitter());
        harness.addToBattlefield(player2, new GrizzlyBears()); // no counter

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, dyingId); // Grizzly Bears dies without a -1/-1 counter
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when the controller's own creature dies with a -1/-1 counter")
    void doesNotTriggerForOwnCreature() {
        harness.addToBattlefield(player1, new Necroskitter());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, dyingId); // player1's own creature dies — Necroskitter must not trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Returns a creature you own that an opponent controlled when it died")
    void returnsOwnCardControlledByOpponent() {
        harness.addToBattlefield(player1, new Necroskitter());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).remove(dying);
        gd.playerBattlefields.get(player2.getId()).add(dying);
        gd.stolenCreatures.put(dying.getId(), player1.getId());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, dying.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Lethal wither damage triggers the return and the returned creature has no counters")
    void lethalWitherReturnsFreshPermanent() {
        Permanent attacker = addCreatureReady(player1, new Necroskitter());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(blocker.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(returned.getMarkedDamage()).isZero();
        assertThat(returned.isSummoningSick()).isTrue();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The return still triggers when Necroskitter dies in the same combat damage event")
    void triggersWhenNecroskitterDiesSimultaneously() {
        Permanent attacker = addCreatureReady(player1, new Necroskitter());
        attacker.setAttacking(true);
        attacker.setMarkedDamage(3);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.assertInGraveyard(player1, "Necroskitter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Necroskitter");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Wither deals normal damage to a player")
    void witherDealsNormalDamageToPlayer() {
        Permanent attacker = addCreatureReady(player1, new Necroskitter());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Returns a creature that dies from zero toughness with several -1/-1 counters")
    void returnsCreatureWithZeroToughness() {
        harness.addToBattlefield(player1, new Necroskitter());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new Necroskitter());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);

        harness.runStateBasedActions();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Necroskitter")).isEqualTo(2);
        assertThat(findPermanents(player1, "Necroskitter"))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero());
        harness.assertNotInGraveyard(player2, "Necroskitter");
        harness.assertNotOnBattlefield(player2, "Necroskitter");
    }

    @Test
    @DisplayName("Uses counters before the simultaneous death and counter cancellation")
    void triggersWhenCountersCancelAsCreatureDies() {
        harness.addToBattlefield(player1, new Necroskitter());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        dying.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
