package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoggedDetective.class})
class DoggedDetectiveTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldSurveilsTwo() {
        Card topCard = new DoggedDetective();
        Card secondCard = new DoggedDetective();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new DoggedDetective()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondCard);
    }

    @Test
    void returnsFromGraveyardWhenOpponentDrawsTheirSecondCard() {
        DoggedDetective detective = new DoggedDetective();
        harness.setGraveyard(player1, List.of(detective));
        harness.setLibrary(player2, List.of(new DoggedDetective(), new DoggedDetective()));

        drawCard(player2);
        assertThat(gd.stack).isEmpty();

        drawCard(player2);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(detective);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(detective);
    }

    @Test
    void doesNotTriggerForItsOwnersDrawOrAnOpponentsFirstDraw() {
        DoggedDetective detective = new DoggedDetective();
        harness.setGraveyard(player1, List.of(detective));
        harness.setLibrary(player1, List.of(new DoggedDetective(), new DoggedDetective()));
        harness.setLibrary(player2, List.of(new DoggedDetective(), new DoggedDetective()));

        drawCard(player1);
        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDeclineTheReturnAndDoesNotTriggerOnTheThirdDraw() {
        DoggedDetective detective = new DoggedDetective();
        harness.setGraveyard(player1, List.of(detective));
        harness.setLibrary(player2, List.of(new DoggedDetective(), new DoggedDetective(), new DoggedDetective()));

        drawCard(player2);
        drawCard(player2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(detective);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(detective);
        drawCard(player2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerFromTheBattlefieldOrHand() {
        harness.addToBattlefield(player1, new DoggedDetective());
        harness.setHand(player1, List.of(new DoggedDetective()));
        harness.setLibrary(player2, List.of(new DoggedDetective(), new DoggedDetective()));

        drawCard(player2);
        drawCard(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotReturnIfItLeavesTheGraveyardBeforeResolution() {
        DoggedDetective detective = new DoggedDetective();
        harness.setGraveyard(player1, List.of(detective));
        harness.setLibrary(player2, List.of(new DoggedDetective(), new DoggedDetective()));
        drawCard(player2);
        drawCard(player2);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(detective));

        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(detective);
        assertThat(gd.findExiledCard(detective.getId())).isNotNull();
    }

    @Test
    void mayKeepBothSurveilledCardsInReverseOrder() {
        Card first = new DoggedDetective();
        Card second = new DoggedDetective();
        Card third = new DoggedDetective();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DoggedDetective()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    void mayPutAllAvailableCardsIntoGraveyardWithAShortLibrary() {
        Card onlyCard = new DoggedDetective();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new DoggedDetective()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
    }

    private void drawCard(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
