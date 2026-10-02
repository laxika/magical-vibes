package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Inspiration;
import com.github.laxika.magicalvibes.cards.u.UginTheSpiritDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AleshaWhoSmilesAtDeath.class, GrizzlyBears.class, HillGiant.class, Inspiration.class, UginTheSpiritDragon.class})
class AleshaWhoSmilesAtDeathTest extends BaseCardTest {

    @Test
    void attackTriggerOnlyOffersCreatureCardsWithPowerAtMostTwo() {
        addCreatureReady(player1, new AleshaWhoSmilesAtDeath());
        Card valid = new GrizzlyBears();
        Card tooPowerful = new HillGiant();
        Card nonCreature = new Inspiration();
        harness.setGraveyard(player1, List.of(valid, tooPowerful, nonCreature));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(valid.getId());
    }

    @Test
    void payingTwoHybridManaReturnsTheChosenCreatureTappedAndAttacking() {
        addCreatureReady(player1, new AleshaWhoSmilesAtDeath());
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void decliningPaymentLeavesTheTargetInTheGraveyard() {
        addCreatureReady(player1, new AleshaWhoSmilesAtDeath());
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    void noMatchingCardMeansTheAttackTriggerIsNotPutOnTheStack() {
        addCreatureReady(player1, new AleshaWhoSmilesAtDeath());
        harness.setGraveyard(player1, List.of(new HillGiant()));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mixedHybridManaReturnsTheCreature() {
        addCreatureReady(player1, new AleshaWhoSmilesAtDeath());
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void twoBlackManaReturnsTheCreature() {
        addCreatureReady(player1, new AleshaWhoSmilesAtDeath());
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentGraveyardCardsAreNotOfferedAsTargets() {
        addCreatureReady(player1, new AleshaWhoSmilesAtDeath());
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());
    }

    @Test
    void returnedCreatureCanAttackAPlaneswalkerInsteadOfAleshasDefender() {
        gd.orderedPlayerIds.forEach(id -> gd.playerAutoStopSteps.put(id,
                java.util.EnumSet.allOf(com.github.laxika.magicalvibes.model.TurnStep.class)));
        addCreatureReady(player1, new AleshaWhoSmilesAtDeath());
        Permanent ugin = harness.addToBattlefieldAndReturn(player2, new UginTheSpiritDragon());
        ugin.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 7);
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(ugin.getId());
        assertThat(choice.validPlayerIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, ugin.getId());
        assertThat(findPermanent(player1, "Grizzly Bears").getAttackTarget()).isEqualTo(ugin.getId());
    }

}
