package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LlanowarEmpathTest extends BaseCardTest {

    private void castLlanowarEmpath() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LlanowarEmpath()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB enters scry state with 2 cards")
    void etbEntersScry2() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new GrizzlyBears()));

        castLlanowarEmpath();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Revealed creature card goes to hand")
    void revealedCreatureGoesToHand() {
        Card top = new GrizzlyBears();
        Card second = new Forest();
        Card rest = new Forest();
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
        Card top = new Forest();
        Card second = new Forest();
        Card rest = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second, rest));

        castLlanowarEmpath();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
    }

    @Test
    @DisplayName("Scry reorder decides which card is revealed")
    void scryReorderDecidesRevealedCard() {
        Card a = new Forest();
        Card b = new GrizzlyBears();
        Card rest = new Forest();
        harness.setLibrary(player1, List.of(a, b, rest));

        castLlanowarEmpath();
        // Put the Grizzly Bears on top, bottom the Forest.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(b);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(b);
    }

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        castLlanowarEmpath();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
