package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoraciousDragon.class, GoblinPiker.class, GrizzlyBears.class, WingsOfVelisVel.class, Unsummon.class})
class VoraciousDragonTest extends BaseCardTest {

    private void castVoraciousDragon(java.util.UUID targetId) {
        harness.setLife(player2, 20);
        harness.setHand(player1, new ArrayList<>(List.of(new VoraciousDragon())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private Permanent dragon() {
        return findPermanent(player1, "Voracious Dragon");
    }

    @Test
    @DisplayName("Devouring two Goblins deals twice their number (4) to any target")
    void devourTwoGoblinsDealsFour() {
        Permanent goblinA = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        Permanent goblinB = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        castVoraciousDragon(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(goblinA.getId(), goblinB.getId()));

        // Devour 1 x 2 creatures = 2 +1/+1 counters.
        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities(); // resolve the ETB damage trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16); // 20 - (2 x 2 Goblins)
    }

    @Test
    @DisplayName("Only Goblins devoured count for the damage; a devoured non-Goblin adds a counter but no damage")
    void onlyGoblinsCountForDamage() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castVoraciousDragon(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        harness.handleMultiplePermanentsChosen(player1, List.of(goblin.getId(), bear.getId()));

        // Both devoured creatures grant counters...
        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities(); // resolve the ETB damage trigger

        // ...but only the single Goblin counts for damage: 2 x 1 = 2.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining to devour deals no damage even with a Goblin available")
    void declineDevourDealsNoDamage() {
        harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        castVoraciousDragon(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities(); // resolve the ETB damage trigger (0 damage)

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A creature that gained all creature types counts as a Goblin when devoured")
    void countsGoblinTypeGainedOnBattlefield() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        castVoraciousDragon(player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering with no other creatures deals no damage and needs no devour choice")
    void noCreaturesToDevour() {
        castVoraciousDragon(player2.getId());
        harness.passBothPriorities();

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The enter trigger can deal lethal damage to an opposing creature")
    void damageCanTargetCreature() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castVoraciousDragon(bear.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(goblin.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning the Dragon to hand does not erase the Goblins its trigger counts")
    void damageUsesLastKnownDevourCount() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        castVoraciousDragon(player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(goblin.getId()));

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, dragon().getId());

        harness.assertNotOnBattlefield(player1, "Voracious Dragon");
        harness.assertInHand(player1, "Voracious Dragon");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }
}
