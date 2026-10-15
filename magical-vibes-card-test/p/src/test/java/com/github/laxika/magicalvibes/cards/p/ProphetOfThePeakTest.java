package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProphetOfThePeak.class, Gingerbrute.class})
class ProphetOfThePeakTest extends BaseCardTest {

    @Test
    void enteringBattlefieldScriesTwo() {
        List<Card> library = List.of(new Gingerbrute(), new Gingerbrute(), new Gingerbrute());
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new ProphetOfThePeak(), "{6}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(library.get(0), library.get(1));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(1), library.get(2), library.get(0));
    }

    @Test
    void canKeepBothCardsOnTopInEitherOrder() {
        List<Card> library = List.of(new Gingerbrute(), new Gingerbrute(), new Gingerbrute());
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new ProphetOfThePeak(), "{6}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(1), library.get(0), library.get(2));
    }

    @Test
    void canPutBothCardsOnBottomInEitherOrder() {
        List<Card> library = List.of(new Gingerbrute(), new Gingerbrute(), new Gingerbrute());
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new ProphetOfThePeak(), "{6}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(2), library.get(1), library.get(0));
    }

    @Test
    void scriesOnlyAvailableCardInOneCardLibrary() {
        Card card = new Gingerbrute();
        harness.setLibrary(player1, List.of(card));
        harness.castFromHand(player1, new ProphetOfThePeak(), "{6}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(card);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotRequireScryChoice() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new ProphetOfThePeak(), "{6}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Prophet of the Peak");
    }
}
