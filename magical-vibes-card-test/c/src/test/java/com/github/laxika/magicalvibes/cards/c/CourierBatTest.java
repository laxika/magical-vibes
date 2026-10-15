package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.cards.b.BloodFountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourierBat.class, DoomedDissenter.class, BloodFountain.class})
class CourierBatTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when you have not gained life this turn")
    void doesNotTriggerWithoutLifeGain() {
        harness.setGraveyard(player1, List.of(new DoomedDissenter()));

        castCourierBat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Doomed Dissenter");
    }

    @Test
    @DisplayName("Returns up to one creature card after life gain")
    void returnsCreatureCardAfterLifeGain() {
        DoomedDissenter creatureCard = new DoomedDissenter();
        harness.setGraveyard(player1, List.of(creatureCard));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        castCourierBat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Doomed Dissenter");
        harness.assertNotInGraveyard(player1, "Doomed Dissenter");
    }

    @Test
    @DisplayName("Does not offer noncreature cards")
    void doesNotOfferNoncreatureCards() {
        harness.setGraveyard(player1, List.of(new BloodFountain()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        castCourierBat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Blood Fountain");
    }

    @Test
    @DisplayName("Only offers creatures from your own graveyard")
    void onlyOffersOwnCreatureCards() {
        DoomedDissenter ownCreature = new DoomedDissenter();
        DoomedDissenter opposingCreature = new DoomedDissenter();
        harness.setGraveyard(player1, List.of(ownCreature, new BloodFountain()));
        harness.setGraveyard(player2, List.of(opposingCreature));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        castCourierBat();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(ownCreature);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Doomed Dissenter");
        harness.assertInGraveyard(player1, "Blood Fountain");
        harness.assertInGraveyard(player2, "Doomed Dissenter");
    }

    @Test
    @DisplayName("Opponent's life gain does not enable the trigger")
    void opponentLifeGainDoesNotEnableTrigger() {
        harness.setGraveyard(player1, List.of(new DoomedDissenter()));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 1));

        castCourierBat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Doomed Dissenter");
    }

    @Test
    @DisplayName("Life gained while the creature spell is on the stack enables its ETB")
    void lifeGainBeforeEnteringEnablesTrigger() {
        DoomedDissenter creature = new DoomedDissenter();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CourierBat(), "{2}{B}");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Doomed Dissenter");
    }

    @Test
    @DisplayName("Life gained after entering does not create an ETB trigger retroactively")
    void lifeGainAfterEnteringDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new DoomedDissenter()));
        castCourierBat();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Doomed Dissenter");
    }

    @Test
    @DisplayName("A target removed before resolution is not replaced by another creature")
    void removedTargetIsNotReplaced() {
        DoomedDissenter target = new DoomedDissenter();
        CourierBat otherCreature = new CourierBat();
        harness.setGraveyard(player1, List.of(target, otherCreature));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        castCourierBat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCreature));

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Doomed Dissenter");
        harness.assertNotInHand(player1, "Courier Bat");
        harness.assertInGraveyard(player1, "Courier Bat");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty graveyard requires no target selection")
    void emptyGraveyardNeedsNoChoice() {
        harness.setGraveyard(player1, List.of());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        castCourierBat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Courier Bat");
    }

    @Test
    @DisplayName("Can decline the optional creature return")
    void canDeclineReturn() {
        harness.setGraveyard(player1, List.of(new DoomedDissenter()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        castCourierBat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Doomed Dissenter");
        harness.assertNotInHand(player1, "Doomed Dissenter");
    }

    private void castCourierBat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CourierBat(), "{2}{B}");
        harness.passBothPriorities();
    }
}
