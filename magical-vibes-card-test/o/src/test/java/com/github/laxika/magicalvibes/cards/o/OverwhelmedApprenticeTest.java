package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OverwhelmedApprentice.class)
class OverwhelmedApprenticeTest extends BaseCardTest {

    @Test
    void millsEachOpponentThenScriesTwo() {
        List<Card> opponentLibrary = List.of(new OverwhelmedApprentice(), new OverwhelmedApprentice(), new OverwhelmedApprentice());
        List<Card> controllerLibrary = List.of(new OverwhelmedApprentice(), new OverwhelmedApprentice(), new OverwhelmedApprentice());
        harness.setLibrary(player2, opponentLibrary);
        harness.setLibrary(player1, controllerLibrary);
        harness.setHand(player1, List.of(new OverwhelmedApprentice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(opponentLibrary.get(0), opponentLibrary.get(1));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(controllerLibrary.get(0), controllerLibrary.get(1));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(controllerLibrary.get(1), controllerLibrary.get(2), controllerLibrary.get(0));
    }

    @Test
    void millsAvailableCardAndScriesWithOnlyOneCardInLibrary() {
        Card opponentCard = new OverwhelmedApprentice();
        Card controllerCard = new OverwhelmedApprentice();
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of(controllerCard));
        harness.setHand(player1, List.of(new OverwhelmedApprentice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(controllerCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void stillScriesWhenOpponentLibraryIsEmptyAndCanReorderBothOnTop() {
        List<Card> library = List.of(new OverwhelmedApprentice(), new OverwhelmedApprentice(),
                new OverwhelmedApprentice());
        harness.setLibrary(player2, List.of());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new OverwhelmedApprentice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(library.get(0), library.get(1));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(1), library.get(0), library.get(2));
    }

    @Test
    void canPutBothScryCardsOnBottomInChosenOrder() {
        List<Card> library = List.of(new OverwhelmedApprentice(), new OverwhelmedApprentice(),
                new OverwhelmedApprentice());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new OverwhelmedApprentice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(2), library.get(1), library.get(0));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
