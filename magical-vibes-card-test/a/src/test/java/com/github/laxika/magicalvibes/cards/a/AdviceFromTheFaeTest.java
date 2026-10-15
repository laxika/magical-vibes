package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdviceFromTheFae.class, SafeholdSentry.class})
class AdviceFromTheFaeTest extends BaseCardTest {

    private List<Card> setupTopFive() {
        List<Card> top = List.of(
                new SafeholdSentry(), new SafeholdSentry(), new SafeholdSentry(),
                new SafeholdSentry(), new SafeholdSentry());
        harness.setLibrary(player1, top);
        harness.setHand(player1, List.of(new AdviceFromTheFae()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        return top;
    }

    @Test
    @DisplayName("Controlling more creatures than each other player keeps two cards")
    void keepsTwoWhenControllingMoreCreatures() {
        addCreatureReady(player1, new SafeholdSentry()); // player1: 1 creature, player2: 0
        List<Card> top = setupTopFive();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(top.get(0).getId(), top.get(1).getId()));
        // The remaining three are ordered onto the bottom of the library.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        assertThat(gd.playerHands.get(player1.getId())).contains(top.get(0), top.get(1));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3)
                .containsExactlyInAnyOrder(top.get(2), top.get(3), top.get(4));
    }

    @Test
    @DisplayName("Not controlling more creatures keeps only one card")
    void keepsOneWhenNotControllingMore() {
        addCreatureReady(player1, new SafeholdSentry());
        addCreatureReady(player2, new SafeholdSentry()); // equal counts -> not "more"
        List<Card> top = setupTopFive();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(top.get(0).getId()));
        // The remaining four are ordered onto the bottom of the library.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        assertThat(gd.playerHands.get(player1.getId())).contains(top.get(0));
        assertThat(gd.playerHands.get(player1.getId()))
                .doesNotContain(top.get(1), top.get(2), top.get(3), top.get(4));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Controlling fewer creatures than another player keeps only one card")
    void keepsOneWhenOpponentControlsMoreCreatures() {
        addCreatureReady(player2, new SafeholdSentry());
        addCreatureReady(player2, new SafeholdSentry());
        List<Card> top = setupTopFive();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(top.get(0).getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        assertThat(gd.playerHands.get(player1.getId())).contains(top.get(0));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .containsExactlyInAnyOrder(top.get(1), top.get(2), top.get(3), top.get(4));
    }

    @Test
    void putsUnchosenCardsBelowUntouchedLibraryInChosenOrder() {
        List<Card> top = setupTopFive();
        Card untouched = new SafeholdSentry();
        List<Card> library = new ArrayList<>(top);
        library.add(untouched);
        harness.setLibrary(player1, library);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(top.get(3).getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 1, 0, 2)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top.get(3));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouched, top.get(4), top.get(1), top.get(0), top.get(2));
    }

    @Test
    void putsAllAvailableCardsIntoHandWhenLibraryHasFewerThanTwo() {
        addCreatureReady(player1, new SafeholdSentry());
        List<Card> top = setupTopFive();
        harness.setLibrary(player1, List.of(top.get(0)));

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top.get(0));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithAnEmptyLibraryWithoutDrawing() {
        setupTopFive();
        harness.setLibrary(player1, List.of());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
