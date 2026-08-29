package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathOfAThousandStingsTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses 1 life and controller gains 1 life")
    void drainsOneLife() {
        harness.setHand(player1, List.of(new DeathOfAThousandStings()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Upkeep trigger is offered with more cards in hand than each opponent")
    void triggersWithMoreCardsInHand() {
        harness.setGraveyard(player1, List.of(new DeathOfAThousandStings()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Does not trigger without more cards in hand than each opponent")
    void doesNotTriggerWithoutHandAdvantage() {
        harness.setGraveyard(player1, List.of(new DeathOfAThousandStings()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting the upkeep trigger returns the card to hand")
    void acceptingTriggerReturnsToHand() {
        DeathOfAThousandStings stings = new DeathOfAThousandStings();
        harness.setGraveyard(player1, List.of(stings));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(stings.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(stings.getId()));
    }

    @Test
    @DisplayName("Condition is rechecked when the upkeep trigger resolves")
    void doesNotReturnAfterLosingHandAdvantage() {
        DeathOfAThousandStings stings = new DeathOfAThousandStings();
        harness.setGraveyard(player1, List.of(stings));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(stings.getId()));
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves the card in the graveyard")
    void decliningTriggerLeavesCardInGraveyard() {
        DeathOfAThousandStings stings = new DeathOfAThousandStings();
        harness.setGraveyard(player1, List.of(stings));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(stings.getId()));
    }
}
