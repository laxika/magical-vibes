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
}
