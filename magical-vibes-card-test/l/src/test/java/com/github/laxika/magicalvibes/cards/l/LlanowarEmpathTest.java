package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.w.Whetwheel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LlanowarEmpath.class, Whetwheel.class})
class LlanowarEmpathTest extends BaseCardTest {

    private void castLlanowarEmpath() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new LlanowarEmpath(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB enters scry state with 2 cards")
    void etbEntersScry2() {
        harness.setLibrary(player1, List.of(new Whetwheel(), new Whetwheel(), new LlanowarEmpath()));

        castLlanowarEmpath();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Revealed creature card goes to hand")
    void revealedCreatureGoesToHand() {
        Card top = new LlanowarEmpath();
        Card second = new Whetwheel();
        Card rest = new Whetwheel();
        harness.setLibrary(player1, List.of(top, second, rest));

        castLlanowarEmpath();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(second);
    }

    @Test
    @DisplayName("Revealed non-creature card stays on top of the library")
    void revealedNonCreatureStaysOnTop() {
        Card top = new Whetwheel();
        Card second = new Whetwheel();
        Card rest = new LlanowarEmpath();
        harness.setLibrary(player1, List.of(top, second, rest));

        castLlanowarEmpath();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
    }

    @Test
    @DisplayName("Scry reorder decides which card is revealed")
    void scryReorderDecidesRevealedCard() {
        Card a = new Whetwheel();
        Card b = new LlanowarEmpath();
        Card rest = new Whetwheel();
        harness.setLibrary(player1, List.of(a, b, rest));

        castLlanowarEmpath();
        // Put the Llanowar Empath on top, bottom the Whetwheel.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(b);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(b);
    }

    @Test
    @DisplayName("Scry 2 looks at a short library before revealing")
    void scryTwoClampsToShortLibrary() {
        Card onlyCard = new LlanowarEmpath();
        harness.setLibrary(player1, List.of(onlyCard));

        castLlanowarEmpath();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        castLlanowarEmpath();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Bottoming both scry cards reveals the previously third card")
    void bottomingBothRevealsThirdCard() {
        Card first = new Whetwheel();
        Card second = new Whetwheel();
        Card third = new LlanowarEmpath();
        harness.setLibrary(player1, List.of(first, second, third));

        castLlanowarEmpath();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bottoming the entire library still reveals its new top card")
    void bottomingEntireLibraryStillRevealsCard() {
        Card first = new Whetwheel();
        Card second = new LlanowarEmpath();
        harness.setLibrary(player1, List.of(first, second));

        castLlanowarEmpath();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Keeping both scry cards permits reordering before the reveal")
    void keepingBothPermitsReordering() {
        Card first = new Whetwheel();
        Card second = new LlanowarEmpath();
        Card third = new Whetwheel();
        harness.setLibrary(player1, List.of(first, second, third));

        castLlanowarEmpath();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
