package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DutifulAttendant.class, GrizzlyBears.class, WrathOfGod.class})
class DutifulAttendantTest extends BaseCardTest {

    @Test
    void returnsAnotherTargetCreatureFromGraveyardToHandWhenItDies() {
        Card attendant = new DutifulAttendant();
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, attendant);
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature)));

        destroyAttendant();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(attendant.getId()));
    }

    @Test
    void doesNotOfferAChoiceWithoutAnotherCreatureInGraveyard() {
        Card attendant = new DutifulAttendant();
        harness.addToBattlefield(player1, attendant);

        destroyAttendant();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(attendant.getId()));
    }

    @Test
    void canReturnACreatureThatDiesAtTheSameTime() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new DutifulAttendant());
        harness.addToBattlefield(player1, creature);

        destroyAttendant();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dutiful Attendant");
    }

    @Test
    void excludesNoncreaturesAndOpponentsCreaturesFromTargets() {
        Card creature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        Card noncreature = new WrathOfGod();
        harness.addToBattlefield(player1, new DutifulAttendant());
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        destroyAttendant();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Wrath of God");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canReturnAnotherDutifulAttendant() {
        Card attendant = new DutifulAttendant();
        Card otherAttendant = new DutifulAttendant();
        harness.addToBattlefield(player1, attendant);
        harness.setGraveyard(player1, List.of(otherAttendant));

        destroyAttendant();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(otherAttendant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(otherAttendant.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId).contains(otherAttendant.getId()).doesNotContain(attendant.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(attendant.getId()).doesNotContain(otherAttendant.getId());
    }

    @Test
    void doesNotChooseANewTargetWhenTheChosenCreatureLeavesTheGraveyard() {
        Card attendant = new DutifulAttendant();
        Card target = new GrizzlyBears();
        Card remainingCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, attendant);
        harness.setGraveyard(player1, List.of(target, remainingCreature));

        destroyAttendant();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(attendant, remainingCreature));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(remainingCreature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
    }

    private void destroyAttendant() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
