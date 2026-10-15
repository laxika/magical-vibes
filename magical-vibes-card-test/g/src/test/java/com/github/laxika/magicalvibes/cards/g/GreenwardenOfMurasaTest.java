package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PlanarOutburst;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenwardenOfMurasa.class, ScourFromExistence.class, PlanarOutburst.class, GraveBirthing.class})
class GreenwardenOfMurasaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a chosen card from its controller's graveyard to hand")
    void enterTriggerReturnsChosenCardFromOwnGraveyard() {
        Card card = new ScourFromExistence();
        harness.setGraveyard(player1, List.of(card));

        castGreenwarden();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Scour from Existence");
        harness.assertNotInGraveyard(player1, "Scour from Existence");
    }

    @Test
    @DisplayName("Declining the ETB return leaves the card in the graveyard")
    void decliningEnterTriggerReturnsNothing() {
        Card card = new ScourFromExistence();
        harness.setGraveyard(player1, List.of(card));

        castGreenwarden();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Scour from Existence");
        harness.assertNotInHand(player1, "Scour from Existence");
    }

    @Test
    @DisplayName("Death trigger may exile Greenwarden and return a chosen card to hand")
    void deathTriggerExilesSourceAndReturnsChosenCard() {
        Card card = new ScourFromExistence();
        harness.setGraveyard(player1, List.of(card));
        Card greenwarden = harness.addToBattlefieldAndReturn(player1, new GreenwardenOfMurasa()).getCard();

        harness.castFromHand(player1, new PlanarOutburst(), "{3}{W}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(card.getId(), greenwarden.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiled -> exiled.getId().equals(greenwarden.getId()));
        harness.assertInHand(player1, "Scour from Existence");
        harness.assertNotInGraveyard(player1, "Scour from Existence");
    }

    @Test
    @DisplayName("Declining the death trigger leaves Greenwarden in the graveyard")
    void decliningDeathTriggerLeavesSourceInGraveyard() {
        Card card = new ScourFromExistence();
        harness.setGraveyard(player1, List.of(card));
        Card greenwarden = harness.addToBattlefieldAndReturn(player1, new GreenwardenOfMurasa()).getCard();

        harness.castFromHand(player1, new PlanarOutburst(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(graveyardCard -> graveyardCard.getId().equals(greenwarden.getId()));
        harness.assertInGraveyard(player1, "Scour from Existence");
    }

    @Test
    @DisplayName("Exiling Greenwarden in response prevents the death trigger from returning its target")
    void deathTriggerCannotReturnCardWhenSourceWasExiledInResponse() {
        Card card = new ScourFromExistence();
        harness.setGraveyard(player1, List.of(card));
        Card greenwarden = harness.addToBattlefieldAndReturn(player1, new GreenwardenOfMurasa()).getCard();

        harness.castFromHand(player1, new PlanarOutburst(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        exileGraveyardCardInResponse(greenwarden);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInGraveyard(player1, "Scour from Existence");
        harness.assertNotInHand(player1, "Scour from Existence");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(greenwarden);
    }

    @Test
    @DisplayName("An illegal death-trigger target prevents Greenwarden from being exiled")
    void deathTriggerDoesNotExileSourceWhenTargetWasExiledInResponse() {
        Card card = new ScourFromExistence();
        harness.setGraveyard(player1, List.of(card));
        Card greenwarden = harness.addToBattlefieldAndReturn(player1, new GreenwardenOfMurasa()).getCard();

        harness.castFromHand(player1, new PlanarOutburst(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        exileGraveyardCardInResponse(card);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(greenwarden);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card).doesNotContain(greenwarden);
        harness.assertNotInHand(player1, "Scour from Existence");
    }

    @Test
    @DisplayName("Greenwarden can target itself on death, but exiling it does not return it to hand")
    void deathTriggerTargetingItselfOnlyExilesIt() {
        Card greenwarden = harness.addToBattlefieldAndReturn(player1, new GreenwardenOfMurasa()).getCard();

        harness.castFromHand(player1, new PlanarOutburst(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(greenwarden.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(greenwarden);
        harness.assertNotInGraveyard(player1, "Greenwarden of Murasa");
        harness.assertNotInHand(player1, "Greenwarden of Murasa");
    }

    @Test
    @DisplayName("ETB targeting excludes cards in an opponent's graveyard")
    void enterTriggerOnlyTargetsControllersGraveyard() {
        Card ownCard = new ScourFromExistence();
        Card opponentCard = new PlanarOutburst();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        castGreenwarden();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Scour from Existence");
        harness.assertInGraveyard(player2, "Planar Outburst");
    }

    @Test
    @DisplayName("ETB has no legal target when only the opponent has cards in their graveyard")
    void enterTriggerWithEmptyOwnGraveyardDoesNothing() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new ScourFromExistence()));

        castGreenwarden();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Greenwarden of Murasa");
        harness.assertInGraveyard(player2, "Scour from Existence");
    }

    @Test
    @DisplayName("ETB does not return a target exiled in response")
    void enterTriggerDoesNothingWhenTargetWasExiledInResponse() {
        Card card = new ScourFromExistence();
        harness.setGraveyard(player1, List.of(card));

        castGreenwarden();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        exileGraveyardCardInResponse(card);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        harness.assertNotInHand(player1, "Scour from Existence");
        harness.assertOnBattlefield(player1, "Greenwarden of Murasa");
    }

    private void exileGraveyardCardInResponse(Card card) {
        harness.setLibrary(player2, List.of(new ScourFromExistence()));
        harness.setHand(player2, List.of(new GraveBirthing()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        int cardIndex = gd.playerGraveyards.get(player1.getId()).indexOf(card);
        assertThat(cardIndex).isNotNegative();
        harness.handleGraveyardCardChosen(player1, cardIndex);
    }

    private void castGreenwarden() {
        harness.castFromHand(player1, new GreenwardenOfMurasa(), "{4}{G}{G}");
        harness.passBothPriorities();
    }
}
