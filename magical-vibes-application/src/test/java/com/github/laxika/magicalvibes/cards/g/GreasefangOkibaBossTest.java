package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BruteSuit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreasefangOkibaBoss.class, BruteSuit.class})
class GreasefangOkibaBossTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a Vehicle from the graveyard with haste and returns it to hand at the next end step")
    void returnsVehicleWithHasteAndReturnsItToHandAtNextEndStep() {
        Card vehicle = new BruteSuit();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Brute Suit");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brute Suit");
        harness.assertInHand(player1, "Brute Suit");
    }

    @Test
    @DisplayName("Cannot target a non-Vehicle card in the graveyard")
    void cannotTargetNonVehicleCard() {
        Card nonVehicle = new GreasefangOkibaBoss();
        harness.setGraveyard(player1, List.of(nonVehicle));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonVehicle);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(nonVehicle.getId()));
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        Card vehicle = new BruteSuit();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());
        harness.forceActivePlayer(player2);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Brute Suit");
    }

    @Test
    @DisplayName("Cannot return a Vehicle from an opponent's graveyard")
    void cannotReturnOpponentsVehicle() {
        harness.setGraveyard(player2, List.of(new BruteSuit()));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Brute Suit");
    }

    @Test
    @DisplayName("Does not return a target that leaves the graveyard before resolution")
    void targetLeavingGraveyardIsNotReturned() {
        Card vehicle = new BruteSuit();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brute Suit");
        harness.assertNotInHand(player1, "Brute Suit");
    }

    @Test
    @DisplayName("The trigger and delayed return survive Greasefang leaving the battlefield")
    void returnDoesNotRequireGreasefangToRemain() {
        Card vehicle = new BruteSuit();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd,
                findPermanent(player1, "Greasefang, Okiba Boss"));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brute Suit");
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Brute Suit");
    }

    @Test
    @DisplayName("Returns only the chosen Vehicle when multiple Vehicles are in the graveyard")
    void returnsOnlyChosenVehicle() {
        Card chosen = new BruteSuit();
        Card other = new BruteSuit();
        Card nonVehicle = new GreasefangOkibaBoss();
        harness.setGraveyard(player1, List.of(chosen, other, nonVehicle));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactlyInAnyOrder(chosen, other);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, nonVehicle);
        assertThat(findPermanent(player1, "Brute Suit").getCard().getId()).isEqualTo(chosen.getId());
    }

    @Test
    @DisplayName("The delayed return does not affect a Vehicle that leaves and reenters the battlefield")
    void delayedReturnDoesNotFollowNewPermanent() {
        Card vehicle = new BruteSuit();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        harness.passBothPriorities();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd,
                findPermanent(player1, "Brute Suit"));
        harness.setGraveyard(player1, List.of());
        harness.addToBattlefield(player1, vehicle);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brute Suit");
        harness.assertNotInHand(player1, "Brute Suit");
    }

    @Test
    @DisplayName("Skipping your end step does not return the Vehicle during an opponent's end step")
    void skippedEndStepWaitsForYourNextEndStep() {
        Card vehicle = new BruteSuit();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Brute Suit");
        harness.assertNotInHand(player1, "Brute Suit");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Brute Suit");
    }
}
