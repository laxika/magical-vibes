package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EternalWitness.class, AuriokChampion.class, WayfarersBauble.class})
class EternalWitnessTest extends BaseCardTest {

    private void castEternalWitness() {
        harness.castFromHand(player1, new EternalWitness(), "{1}{G}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a chosen card from its controller's graveyard to hand")
    void returnsChosenCardFromOwnGraveyard() {
        Card card = new WayfarersBauble();
        harness.setGraveyard(player1, List.of(card));

        castEternalWitness();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Wayfarer's Bauble");
        harness.assertNotInGraveyard(player1, "Wayfarer's Bauble");
    }

    @Test
    @DisplayName("ETB can return any card type")
    void returnsCreatureCard() {
        Card card = new AuriokChampion();
        harness.setGraveyard(player1, List.of(card));

        castEternalWitness();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Auriok Champion");
    }

    @Test
    @DisplayName("Declining the optional return leaves the card in the graveyard")
    void decliningReturnsNothing() {
        Card card = new WayfarersBauble();
        harness.setGraveyard(player1, List.of(card));

        castEternalWitness();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Wayfarer's Bauble");
        harness.assertNotInHand(player1, "Wayfarer's Bauble");
    }

    @Test
    @DisplayName("ETB does not target cards in an opponent's graveyard")
    void onlyTargetsOwnGraveyard() {
        Card card = new WayfarersBauble();
        harness.setGraveyard(player2, List.of(card));

        castEternalWitness();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Wayfarer's Bauble");
    }

    @Test
    @DisplayName("ETB fizzles if the targeted card leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card card = new WayfarersBauble();
        harness.setGraveyard(player1, List.of(card));

        castEternalWitness();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(handCard -> handCard.getId().equals(card.getId()));
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
