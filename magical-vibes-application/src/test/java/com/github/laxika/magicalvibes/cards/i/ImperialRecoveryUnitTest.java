package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.e.EiganjoExemplar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HighSpeedHoverbike;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ImperialRecoveryUnit.class,
        HighSpeedHoverbike.class,
        EiganjoExemplar.class,
        Forest.class,
        BearerOfMemory.class
})
class ImperialRecoveryUnitTest extends BaseCardTest {

    @Test
    void attackTargetsCreatureOrVehicleWithManaValueTwoOrLess() {
        Card creature = new EiganjoExemplar();
        Card vehicle = new HighSpeedHoverbike();
        Card land = new Forest();
        Card expensiveCreature = new BearerOfMemory();
        harness.setGraveyard(player1, List.of(creature, vehicle, land, expensiveCreature));
        prepareUnitForAttack();

        declareAttack();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), vehicle.getId());
    }

    @Test
    void attackReturnsChosenCardToHand() {
        Card creature = new EiganjoExemplar();
        harness.setGraveyard(player1, List.of(creature));
        prepareUnitForAttack();

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Eiganjo Exemplar");
    }

    @Test
    void doesNotTriggerWithoutAnEligibleGraveyardCard() {
        harness.setGraveyard(player1, List.of(new Forest(), new BearerOfMemory()));
        prepareUnitForAttack();

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Bearer of Memory");
    }

    @Test
    void attackReturnsVehicleToHandWithoutReturningAnotherCard() {
        Card vehicle = new HighSpeedHoverbike();
        Card creature = new EiganjoExemplar();
        harness.setGraveyard(player1, List.of(vehicle, creature));
        prepareUnitForAttack();

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "High-Speed Hoverbike");
        harness.assertNotInGraveyard(player1, "High-Speed Hoverbike");
        harness.assertNotOnBattlefield(player1, "High-Speed Hoverbike");
        harness.assertInGraveyard(player1, "Eiganjo Exemplar");
    }

    @Test
    void attackCannotTargetCardsInOpponentsGraveyard() {
        Card ownCard = new EiganjoExemplar();
        Card opposingCard = new HighSpeedHoverbike();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        prepareUnitForAttack();

        declareAttack();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());
    }

    @Test
    void targetLeavingGraveyardDoesNotReturnAnotherEligibleCard() {
        Card target = new HighSpeedHoverbike();
        Card otherCard = new EiganjoExemplar();
        harness.setGraveyard(player1, List.of(target, otherCard));
        prepareUnitForAttack();

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCard));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "High-Speed Hoverbike");
        harness.assertNotInHand(player1, "Eiganjo Exemplar");
        harness.assertInGraveyard(player1, "Eiganjo Exemplar");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void summoningSickCreatureCanCrewAndAnimationEndsWithTheTurn() {
        Permanent unit = addCreatureReady(player1, new ImperialRecoveryUnit());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new EiganjoExemplar());
        assertThat(gqs.isCreature(gd, unit)).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(unit.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, unit)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, unit)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, unit)).isFalse();
    }

    @Test
    void cannotCrewUsingOnlyAnOpponentsCreature() {
        addCreatureReady(player1, new ImperialRecoveryUnit());
        Permanent opposingCreature = addCreatureReady(player2, new EiganjoExemplar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    private void prepareUnitForAttack() {
        Permanent unit = addCreatureReady(player1, new ImperialRecoveryUnit());
        addCreatureReady(player1, new EiganjoExemplar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, unit)).isTrue();
    }

    private void declareAttack() {
        declareAttackers(player1, List.of(0));
    }
}
