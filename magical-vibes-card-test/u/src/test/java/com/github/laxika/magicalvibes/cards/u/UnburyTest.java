package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Unbury.class, GrizzlyBears.class, YoungWolf.class})
class UnburyTest extends BaseCardTest {

    @Test
    @DisplayName("The single-card mode returns a creature card from the graveyard to hand")
    void returnsOneCreatureCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Unbury()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        List<java.util.UUID> targets = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        harness.handleMultipleCardsChosen(player1, targets);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The two-card mode returns two creature cards that share a creature type")
    void returnsTwoCreaturesSharingType() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        Card wolf = new YoungWolf();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, wolf));
        harness.setHand(player1, List.of(new Unbury()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstant(player1, 0, 1, List.of());

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(firstBear.getId(), secondBear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Young Wolf");
    }

    @Test
    @DisplayName("The shared-type mode cannot be cast without a legal creature pair")
    void requiresASharedTypePair() {
        Card bear = new GrizzlyBears();
        Card wolf = new YoungWolf();
        Card spell = new Unbury();
        harness.setGraveyard(player1, List.of(bear, wolf));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Unbury");
    }

    @Test
    @DisplayName("The single-card mode requires a creature in your own graveyard")
    void singleModeRejectsNoncreaturesAndOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new Unbury()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Unbury()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Unbury");
    }

    @Test
    @DisplayName("Two individually eligible targets must share a creature type with each other")
    void rejectsPairOfDifferentTypesEvenWhenEachHasAPartner() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        Card firstWolf = new YoungWolf();
        Card secondWolf = new YoungWolf();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, firstWolf, secondWolf));
        harness.setHand(player1, List.of(new Unbury()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castModalInstant(player1, 0, 1, List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstBear.getId(), firstWolf.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(firstWolf.getId(), secondWolf.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstWolf, secondWolf);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Young Wolf");
    }

    @Test
    @DisplayName("The shared-type mode requires two selected cards")
    void rejectsSelectingOnlyOneCardForTwoCardMode() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBear, secondBear));
        harness.setHand(player1, List.of(new Unbury()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castModalInstant(player1, 0, 1, List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstBear, secondBear);
    }

    @Test
    @DisplayName("The shared-type mode still returns the remaining legal target if one leaves the graveyard")
    void returnsRemainingTargetWhenOneTargetLeavesGraveyard() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBear, secondBear));
        harness.setHand(player1, List.of(new Unbury()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));

        harness.setGraveyard(player1, List.of(secondBear));
        harness.setExile(player1, List.of(firstBear));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondBear);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
}
