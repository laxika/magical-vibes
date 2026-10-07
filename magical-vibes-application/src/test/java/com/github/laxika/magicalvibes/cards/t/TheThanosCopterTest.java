package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NeurokTransmuter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheThanosCopter.class, DuskLegionDreadnought.class, GrizzlyBears.class,
        NeurokTransmuter.class})
class TheThanosCopterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB animates your Vehicles until end of turn")
    void entersAndAnimatesOwnVehicles() {
        Permanent ownVehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());

        Permanent thanosCopter = harness.enterBattlefieldAndReturn(player1, new TheThanosCopter());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isTrue();
        assertThat(gqs.isCreature(gd, thanosCopter)).isTrue();
        assertThat(gqs.isCreature(gd, opponentVehicle)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isFalse();
        assertThat(gqs.isCreature(gd, thanosCopter)).isFalse();
    }

    @Test
    @DisplayName("Crew 2 animates The Thanos-Copter")
    void crewsWithTwoPower() {
        Permanent thanosCopter = harness.addToBattlefieldAndReturn(player1, new TheThanosCopter());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, thanosCopter)).isTrue();
        assertThat(gqs.getEffectivePower(gd, thanosCopter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thanosCopter)).isEqualTo(5);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Vehicle dealing combat damage draws a card, but a non-Vehicle does not")
    void vehicleCombatDamageDraws() {
        Permanent thanosCopter = harness.addToBattlefieldAndReturn(player1, new TheThanosCopter());
        thanosCopter.setSummoningSick(false);
        thanosCopter.setAnimatedUntilEndOfTurn(true);
        thanosCopter.setAnimatedPower(5);
        thanosCopter.setAnimatedToughness(5);
        thanosCopter.setAttacking(true);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Entry restores the artifact type of a Vehicle that lost it")
    void entryRestoresArtifactType() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, vehicle.getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(gd, vehicle)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new TheThanosCopter());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, vehicle)).isTrue();
    }

    @Test
    @DisplayName("Entry animation does not affect Vehicles arriving after resolution")
    void laterVehiclesAreNotAnimated() {
        harness.enterBattlefieldAndReturn(player1, new TheThanosCopter());
        harness.passBothPriorities();

        Permanent laterVehicle = harness.enterBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, laterVehicle)).isFalse();
    }

    @Test
    @DisplayName("Each Vehicle dealing combat damage draws separately")
    void drawsForEachVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        vehicle.setSummoningSick(false);
        Permanent copter = harness.enterBattlefieldAndReturn(player1, new TheThanosCopter());
        harness.passBothPriorities();
        vehicle.setAttacking(true);
        copter.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("An opposing Vehicle dealing combat damage does not draw for you")
    void opposingVehicleDoesNotDraw() {
        harness.addToBattlefield(player1, new TheThanosCopter());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        vehicle.setSummoningSick(false);
        vehicle.setAnimatedUntilEndOfTurn(true);
        vehicle.setAnimatedPower(4);
        vehicle.setAnimatedToughness(6);
        vehicle.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Crew restores the artifact type after a Vehicle loses it")
    void crewRestoresArtifactType() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        Permanent copter = harness.enterBattlefieldAndReturn(player1, new TheThanosCopter());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, copter.getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(gd, copter)).isFalse();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, copter)).isTrue();
        assertThat(gqs.isArtifact(gd, copter)).isTrue();
    }
}
