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

    @Test
    @DisplayName("An empty graveyard produces no target or optional-return prompt")
    void emptyGraveyardDoesNotPrompt() {
        harness.setGraveyard(player1, List.of());

        castEternalWitness();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Eternal Witness");
    }

    @Test
    @DisplayName("Only the selected card returns when several cards are eligible")
    void returnsOnlySelectedCard() {
        Card artifact = new WayfarersBauble();
        Card creature = new AuriokChampion();
        Card opponentsCard = new WayfarersBauble();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setGraveyard(player2, List.of(opponentsCard));

        castEternalWitness();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), creature.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Auriok Champion");
        harness.assertNotInGraveyard(player1, "Auriok Champion");
        harness.assertInGraveyard(player1, "Wayfarer's Bauble");
        harness.assertNotInHand(player1, "Wayfarer's Bauble");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    @DisplayName("The return uses the entering creature's controller's graveyard and hand")
    void secondPlayerReturnsTheirOwnCard() {
        Card ownCard = new WayfarersBauble();
        Card opponentsCard = new AuriokChampion();
        harness.setGraveyard(player2, List.of(ownCard));
        harness.setGraveyard(player1, List.of(opponentsCard));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new EternalWitness(), "{1}{G}{G}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());

        harness.handleMultipleCardsChosen(player2, List.of(ownCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInHand(player2, "Wayfarer's Bauble");
        harness.assertNotInGraveyard(player2, "Wayfarer's Bauble");
        harness.assertNotInHand(player1, "Wayfarer's Bauble");
        harness.assertInGraveyard(player1, "Auriok Champion");
    }

    @Test
    @DisplayName("Entering without being cast still triggers the optional return")
    void enteringWithoutCastingReturnsCard() {
        Card card = new WayfarersBauble();
        harness.setGraveyard(player1, List.of(card));

        harness.enterBattlefieldAndReturn(player1, new EternalWitness());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Eternal Witness");
        harness.assertInHand(player1, "Wayfarer's Bauble");
        harness.assertNotInGraveyard(player1, "Wayfarer's Bauble");
    }
}
