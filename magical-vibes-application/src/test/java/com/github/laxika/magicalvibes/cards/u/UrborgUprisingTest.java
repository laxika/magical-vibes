package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DegaDisciple;
import com.github.laxika.magicalvibes.cards.j.JadedResponse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrborgUprising.class, DegaDisciple.class, JadedResponse.class})
class UrborgUprisingTest extends BaseCardTest {

    @Test
    void returnsUpToTwoCreaturesAndDrawsACard() {
        Card creature1 = new DegaDisciple();
        Card creature2 = new DegaDisciple();
        Card drawnCard = new JadedResponse();
        harness.setGraveyard(player1, List.of(creature1, creature2, new JadedResponse()));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new UrborgUprising(), "{4}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(creature1.getId(), creature2.getId(), drawnCard.getId());
        harness.assertInGraveyard(player1, "Urborg Uprising");
    }

    @Test
    void onlyCreatureCardsCanBeReturned() {
        Card creature = new DegaDisciple();
        Card noncreature = new JadedResponse();
        harness.setGraveyard(player1, List.of(creature, noncreature));

        harness.castFromHand(player1, new UrborgUprising(), "{4}{B}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    void choosingNoCreaturesStillDrawsACard() {
        Card creature = new DegaDisciple();
        Card drawnCard = new JadedResponse();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new UrborgUprising(), "{4}{B}");
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(drawnCard.getId());
        harness.assertInGraveyard(player1, "Dega Disciple");
    }

    @Test
    void returningOnlyOneCreatureStillDrawsACard() {
        Card creature = new DegaDisciple();
        Card drawnCard = new JadedResponse();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new UrborgUprising(), "{4}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), drawnCard.getId());
        harness.assertNotInGraveyard(player1, "Dega Disciple");
    }

    @Test
    void noCreatureCardsInGraveyardStillAllowsTheDraw() {
        Card noncreature = new JadedResponse();
        Card drawnCard = new JadedResponse();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new UrborgUprising(), "{4}{B}");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(drawnCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(noncreature.getId());
    }

    @Test
    void onlyTheControllersGraveyardCanBeChosen() {
        Card ownCreature = new DegaDisciple();
        Card opponentCreature = new DegaDisciple();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        harness.castFromHand(player1, new UrborgUprising(), "{4}{B}");

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dega Disciple");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .containsExactly(opponentCreature.getId());
    }

    @Test
    void oneRemainingLegalTargetIsReturnedAndACardIsDrawn() {
        Card removedCreature = new DegaDisciple();
        Card remainingCreature = new DegaDisciple();
        Card drawnCard = new JadedResponse();
        harness.setGraveyard(player1, List.of(removedCreature, remainingCreature));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new UrborgUprising(), "{4}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(removedCreature.getId(), remainingCreature.getId()));
        harness.setGraveyard(player1, List.of(remainingCreature));
        harness.setHand(player1, List.of(removedCreature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(removedCreature.getId(), remainingCreature.getId(), drawnCard.getId());
        harness.assertNotInGraveyard(player1, "Dega Disciple");
        harness.assertInGraveyard(player1, "Urborg Uprising");
    }

    @Test
    void allTargetsLeavingTheGraveyardPreventsTheDraw() {
        Card creature1 = new DegaDisciple();
        Card creature2 = new DegaDisciple();
        Card libraryCard = new JadedResponse();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.castFromHand(player1, new UrborgUprising(), "{4}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(creature1, creature2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryCard.getId());
        harness.assertInGraveyard(player1, "Urborg Uprising");
    }

    @Test
    void cannotChooseMoreThanTwoCreatureCards() {
        Card creature1 = new DegaDisciple();
        Card creature2 = new DegaDisciple();
        Card creature3 = new DegaDisciple();
        harness.setGraveyard(player1, List.of(creature1, creature2, creature3));

        harness.castFromHand(player1, new UrborgUprising(), "{4}{B}");

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(creature1.getId(), creature2.getId(), creature3.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
