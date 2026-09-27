package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheThanosCopter.class, DuskLegionDreadnought.class, GrizzlyBears.class})
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
