package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrimReaperLethalLegionnaire.class, GrizzlyBears.class, Shock.class})
class GrimReaperLethalLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("Payment creates a separate return trigger that returns the creature tapped and attacking with finality")
    void returnsTargetCreatureTappedAndAttackingWithFinalityCounter() {
        addCreatureReady(player1, new GrimReaperLethalLegionnaire());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addPaymentMana();

        declareAttackers(List.of(0));

        assertNoGraveyardTargetChoice();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        chooseReturnTarget(creature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining payment leaves the creature in the graveyard without choosing a target")
    void decliningPaymentLeavesCreatureInGraveyard() {
        addCreatureReady(player1, new GrimReaperLethalLegionnaire());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addPaymentMana();

        declareAttackers(List.of(0));

        assertNoGraveyardTargetChoice();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertNoGraveyardTargetChoice();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Payment is offered even when the graveyard contains only a noncreature card")
    void onlyTargetsCreatureCards() {
        addCreatureReady(player1, new GrimReaperLethalLegionnaire());
        Card noncreature = new Shock();
        harness.setGraveyard(player1, List.of(noncreature));
        addPaymentMana();

        declareAttackers(List.of(0));

        assertNoGraveyardTargetChoice();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertNoGraveyardTargetChoice();
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        harness.assertNotOnBattlefield(player1, "Shock");
    }

    @Test
    @DisplayName("A creature that dies in response to the attack trigger can be targeted after payment")
    void canReturnCreatureThatDiesBeforePayment() {
        addCreatureReady(player1, new GrimReaperLethalLegionnaire());
        Card creature = new GrizzlyBears();
        Permanent bears = addCreatureReady(player1, creature);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        addPaymentMana();

        declareAttackers(List.of(0));

        assertNoGraveyardTargetChoice();
        assertThat(gd.stack).isNotEmpty();
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        chooseReturnTarget(creature);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A returned creature with a finality counter is exiled when it would die")
    void returnedCreatureIsExiledInsteadOfDying() {
        addCreatureReady(player1, new GrimReaperLethalLegionnaire());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        addPaymentMana();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        chooseReturnTarget(creature);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Grizzly Bears").getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("An opponent's creature card cannot be returned by the attack trigger")
    void cannotReturnCreatureFromOpponentsGraveyard() {
        addCreatureReady(player1, new GrimReaperLethalLegionnaire());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        addPaymentMana();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertNoGraveyardTargetChoice();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void addPaymentMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void assertNoGraveyardTargetChoice() {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    private void chooseReturnTarget(Card creature) {
        if (gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class) != null) {
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        } else if (gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class) != null) {
            harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));
        }
    }
}
