package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.cards.s.StalkingDrone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseChurn.class, Wastes.class, StalkingDrone.class})
class CorpseChurnTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards, then returns a chosen creature card to hand")
    void millsThenReturnsCreature() {
        Card firstMilled = new Wastes();
        Card secondMilled = new Wastes();
        Card creature = new StalkingDrone();
        cast(List.of(firstMilled, secondMilled, creature));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, indexOfCard(creature));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMilled, secondMilled)
                .doesNotContain(creature);
    }

    @Test
    @DisplayName("Declining the return leaves the milled cards in the graveyard")
    void decliningReturnLeavesMilledCardsInGraveyard() {
        Card firstMilled = new Wastes();
        Card secondMilled = new Wastes();
        Card thirdMilled = new Wastes();
        cast(List.of(firstMilled, secondMilled, thirdMilled));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMilled, secondMilled, thirdMilled);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(firstMilled, secondMilled, thirdMilled);
    }

    @Test
    @DisplayName("Only creature cards are offered for the optional return")
    void onlyCreatureCardsCanBeReturned() {
        Card nonCreature = new CorpseChurn();
        Card creature = new StalkingDrone();
        harness.setGraveyard(player1, List.of(nonCreature, creature));
        cast(List.of(new Wastes(), new Wastes(), new Wastes()));

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(indexOfCard(creature));
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new CorpseChurn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
    }

    private int indexOfCard(Card card) {
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getId().equals(card.getId())) {
                return i;
            }
        }
        throw new AssertionError("Card not found in graveyard: " + card.getId());
    }
    @Test
    void millsThreeThenReturnsCreatureFromGraveyard() {
        StalkingDrone creature = new StalkingDrone();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Wastes(), new Wastes(), new Wastes()));
        castAndResolve();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        int creatureIndex = choice.validIndices().stream()
                .filter(index -> gd.playerGraveyards.get(player1.getId()).get(index).getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        harness.handleGraveyardCardChosen(player1, creatureIndex);

        harness.assertInHand(player1, "Stalking Drone");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    void mayDeclineCreatureReturnAfterMilling() {
        StalkingDrone creature = new StalkingDrone();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Wastes(), new Wastes(), new Wastes()));
        castAndResolve();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        harness.assertNotInHand(player1, "Stalking Drone");
    }

    @Test
    void acceptingReturnWithNoCreatureFinishesWithoutCardChoice() {
        harness.setLibrary(player1, List.of(new Wastes(), new Wastes(), new Wastes()));
        castAndResolve();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    void shortLibraryStillAllowsReturningMilledCreature() {
        Card land = new Wastes();
        Card creature = new StalkingDrone();
        cast(List.of(land, creature));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, creature);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, indexOfCard(creature));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land).doesNotContain(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryStillAllowsReturningExistingCreature() {
        Card creature = new StalkingDrone();
        harness.setGraveyard(player1, List.of(creature));
        cast(List.of());

        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, indexOfCard(creature));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void millsOnlyThreeAndOffersOnlyControllersCreatures() {
        Card existingCreature = new StalkingDrone();
        Card milledCreature = new StalkingDrone();
        Card opponentCreature = new StalkingDrone();
        Card fourthCard = new StalkingDrone();
        Card opponentLibraryCard = new Wastes();
        harness.setGraveyard(player1, List.of(existingCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        cast(List.of(new Wastes(), milledCreature, new Wastes(), fourthCard));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourthCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(
                indexOfCard(existingCreature), indexOfCard(milledCreature));
        harness.handleGraveyardCardChosen(player1, indexOfCard(existingCreature));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(existingCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CorpseChurn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
    }
}
