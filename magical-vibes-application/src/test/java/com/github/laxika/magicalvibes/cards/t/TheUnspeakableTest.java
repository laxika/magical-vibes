package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ConsumingVortex;
import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheUnspeakable.class, ConsumingVortex.class, CallousDeceiver.class})
class TheUnspeakableTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player returns a chosen Arcane card from graveyard to hand")
    void combatDamageReturnsArcaneCard() {
        addAttackingUnspeakable(player1);
        ConsumingVortex arcane = new ConsumingVortex();
        CallousDeceiver nonArcane = new CallousDeceiver();
        harness.setGraveyard(player1, List.of(nonArcane, arcane));

        resolveCombat();
        resolveAllTriggers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(arcane.getId());
        harness.handleMultipleCardsChosen(player1, List.of(arcane.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Consuming Vortex");
        harness.assertNotInGraveyard(player1, "Consuming Vortex");
    }

    @Test
    @DisplayName("The return is optional - the controller may decline")
    void controllerMayDecline() {
        addAttackingUnspeakable(player1);
        ConsumingVortex arcane = new ConsumingVortex();
        harness.setGraveyard(player1, List.of(arcane));

        resolveCombat();
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(arcane.getId()));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Consuming Vortex");
    }

    @Test
    @DisplayName("Non-Arcane cards in the graveyard are not offered")
    void nonArcaneCardsNotOffered() {
        addAttackingUnspeakable(player1);
        harness.setGraveyard(player1, List.of(new CallousDeceiver()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Callous Deceiver");
    }

    @Test
    @DisplayName("Trample damage through a blocker still triggers the return")
    void tramplesThroughBlockerAndTriggers() {
        addAttackingUnspeakable(player1);
        Permanent blocker = addCreatureReady(player2, new CallousDeceiver());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        ConsumingVortex arcane = new ConsumingVortex();
        harness.setGraveyard(player1, List.of(arcane));

        resolveCombat();

        var assignment = gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(assignment).isNotNull();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                assignment.validTargets().get(0).id(), 3,
                assignment.validTargets().get(1).id(), 3));
        resolveAllTriggers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(arcane.getId());
        harness.handleMultipleCardsChosen(player1, List.of(arcane.getId()));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Consuming Vortex");
    }

    @Test
    @DisplayName("A targeted Arcane card leaving the graveyard before resolution is not returned")
    void targetLeavingGraveyardBeforeResolutionFizzesTheTrigger() {
        addAttackingUnspeakable(player1);
        ConsumingVortex arcane = new ConsumingVortex();
        harness.setGraveyard(player1, List.of(arcane));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(arcane.getId()));
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Consuming Vortex");
    }

    private Permanent addAttackingUnspeakable(Player player) {
        Permanent unspeakable = addCreatureReady(player, new TheUnspeakable());
        unspeakable.setAttacking(true);
        return unspeakable;
    }

}
