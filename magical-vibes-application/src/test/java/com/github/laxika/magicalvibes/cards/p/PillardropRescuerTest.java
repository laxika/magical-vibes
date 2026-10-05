package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.cards.s.StoneboundMentor;
import com.github.laxika.magicalvibes.cards.s.StudyBreak;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PillardropRescuer.class, EagerFirstYear.class, PillardropWarden.class, StudyBreak.class, StoneboundMentor.class})
class PillardropRescuerTest extends BaseCardTest {

    private void castPillardropRescuer() {
        harness.castFromHand(player1, new PillardropRescuer(), "{4}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted creature card with mana value 3 or less to hand")
    void returnsEligibleCreatureToHand() {
        Card creature = new EagerFirstYear();
        harness.setGraveyard(player1, List.of(creature));

        castPillardropRescuer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Eager First-Year");
        harness.assertNotInGraveyard(player1, "Eager First-Year");
    }

    @Test
    @DisplayName("ETB only targets own creature cards with mana value 3 or less")
    void filtersGraveyardTargets() {
        Card eligible = new EagerFirstYear();
        Card nonCreature = new StudyBreak();
        Card tooExpensive = new PillardropWarden();
        Card opponentCreature = new EagerFirstYear();
        harness.setGraveyard(player1, List.of(eligible, nonCreature, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castPillardropRescuer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    @Test
    @DisplayName("ETB does nothing when no eligible creature card is in the graveyard")
    void noEligibleCreatureProducesNoChoice() {
        harness.setGraveyard(player1, List.of(new StudyBreak(), new PillardropWarden()));

        castPillardropRescuer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB can return a creature with mana value exactly three and returns only the chosen card")
    void returnsManaValueThreeCreatureWithoutReturningOtherEligibleCards() {
        Card chosen = new StoneboundMentor();
        Card other = new EagerFirstYear();
        harness.setGraveyard(player1, List.of(chosen, other));

        castPillardropRescuer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), other.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.assertNotInHand(player1, "Stonebound Mentor");
        harness.assertInGraveyard(player1, "Stonebound Mentor");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Stonebound Mentor");
        harness.assertNotInGraveyard(player1, "Stonebound Mentor");
        harness.assertInGraveyard(player1, "Eager First-Year");
        harness.assertNotInHand(player1, "Eager First-Year");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard before resolution")
    void doesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        Card target = new StoneboundMentor();
        Card other = new EagerFirstYear();
        harness.setGraveyard(player1, List.of(target, other));

        castPillardropRescuer();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Stonebound Mentor");
        harness.assertNotInHand(player1, "Eager First-Year");
        harness.assertInGraveyard(player1, "Eager First-Year");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB cannot return an opponent's creature when its controller's graveyard is empty")
    void emptyOwnGraveyardDoesNotAllowOpponentCreature() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new EagerFirstYear()));

        castPillardropRescuer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Pillardrop Rescuer");
        harness.assertNotInHand(player1, "Eager First-Year");
        harness.assertInGraveyard(player2, "Eager First-Year");
    }
}
