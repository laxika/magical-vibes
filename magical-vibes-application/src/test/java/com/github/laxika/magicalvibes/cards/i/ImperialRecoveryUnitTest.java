package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FuneralLongboat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        ImperialRecoveryUnit.class,
        FuneralLongboat.class,
        GrizzlyBears.class,
        Forest.class,
        ThunderingGiant.class
})
class ImperialRecoveryUnitTest extends BaseCardTest {

    @Test
    void attackTargetsCreatureOrVehicleWithManaValueTwoOrLess() {
        Card creature = new GrizzlyBears();
        Card vehicle = new FuneralLongboat();
        Card land = new Forest();
        Card expensiveCreature = new ThunderingGiant();
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
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        prepareUnitForAttack();

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotTriggerWithoutAnEligibleGraveyardCard() {
        harness.setGraveyard(player1, List.of(new Forest(), new ThunderingGiant()));
        prepareUnitForAttack();

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Thundering Giant");
    }

    private void prepareUnitForAttack() {
        Permanent unit = addCreatureReady(player1, new ImperialRecoveryUnit());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, unit)).isTrue();
    }

    private void declareAttack() {
        declareAttackers(player1, List.of(0));
    }
}
