package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Deliberate.class, Island.class, Mountain.class})
class DeliberateTest extends BaseCardTest {

    @Test
    void scriesTwoThenDrawsTheCardKeptOnTop() {
        Island bottomCard = new Island();
        Mountain topCard = new Mountain();
        Deliberate nextCard = new Deliberate();
        harness.setLibrary(player1, List.of(bottomCard, topCard, nextCard));
        harness.setHand(player1, List.of(new Deliberate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(bottomCard, topCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, bottomCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Deliberate");
    }

    @Test
    void keepsBothCardsInChosenOrderBeforeDrawing() {
        Island firstCard = new Island();
        Mountain secondCard = new Mountain();
        Deliberate thirdCard = new Deliberate();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.setHand(player1, List.of(new Deliberate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstCard, thirdCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Deliberate");
    }

    @Test
    void bottomsBothCardsInChosenOrderThenDrawsTheThirdCard() {
        Island firstCard = new Island();
        Mountain secondCard = new Mountain();
        Deliberate thirdCard = new Deliberate();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.setHand(player1, List.of(new Deliberate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(thirdCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, firstCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Deliberate");
    }

    @Test
    void scriesOnlyTheAvailableCardAndDrawsItEvenWhenBottomed() {
        Island onlyCard = new Island();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new Deliberate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Deliberate");
    }
}
