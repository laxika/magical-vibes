package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoggedDetective.class, GrizzlyBears.class})
class DoggedDetectiveTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldSurveilsTwo() {
        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
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
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        drawCard(player2);
        assertThat(gd.stack).isEmpty();

        drawCard(player2);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(detective);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(detective);
    }

    @Test
    void doesNotTriggerForItsOwnersDrawOrAnOpponentsFirstDraw() {
        DoggedDetective detective = new DoggedDetective();
        harness.setGraveyard(player1, List.of(detective));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        drawCard(player1);
        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player2);
        assertThat(gd.stack).isEmpty();
    }

    private void drawCard(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
