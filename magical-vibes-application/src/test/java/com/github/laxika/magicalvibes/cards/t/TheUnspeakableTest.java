package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ConsumingVortex;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TheUnspeakableTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player returns a chosen Arcane card from graveyard to hand")
    void combatDamageReturnsArcaneCard() {
        addAttackingUnspeakable(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new ConsumingVortex())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Consuming Vortex");
        harness.assertNotInGraveyard(player1, "Consuming Vortex");
    }

    @Test
    @DisplayName("The return is optional - the controller may decline")
    void controllerMayDecline() {
        addAttackingUnspeakable(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new ConsumingVortex())));

        resolveCombatAndTrigger();

        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Consuming Vortex");
    }

    @Test
    @DisplayName("Non-Arcane cards in the graveyard are not offered")
    void nonArcaneCardsNotOffered() {
        addAttackingUnspeakable(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Trample damage through a blocker still triggers the return")
    void tramplesThroughBlockerAndTriggers() {
        addAttackingUnspeakable(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new ConsumingVortex())));

        resolveCombat();

        var assignment = gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(assignment).isNotNull();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                assignment.validTargets().get(0).id(), 2,
                assignment.validTargets().get(1).id(), 4));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Consuming Vortex");
    }

    private Permanent addAttackingUnspeakable(Player player) {
        Permanent unspeakable = addCreatureReady(player, new TheUnspeakable());
        unspeakable.setAttacking(true);
        return unspeakable;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
