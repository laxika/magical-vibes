package com.github.laxika.magicalvibes.cards.n;

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

class NissasRevelationTest extends BaseCardTest {

    private void castNissasRevelation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NissasRevelation()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
    }

    private void keepAllOnTop() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2, 3, 4), List.of()));
    }

    @Test
    @DisplayName("Resolving enters a scry 5 interaction")
    void entersScryFive() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new GrizzlyBears()));

        castNissasRevelation();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("Revealed creature draws cards equal to its power and gains life equal to its toughness")
    void revealedCreatureDrawsAndGainsLife() {
        Card top = new GrizzlyBears();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(top, second, new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();
        keepAllOnTop();

        // 2/2: draws 2 (the revealed card itself first, since it stays on top) and gains 2 life.
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Revealed non-creature stays on top with no draw and no life gain")
    void revealedNonCreatureDoesNothing() {
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top, new Forest(), new Forest(), new Forest(),
                new Forest(), new GrizzlyBears()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();
        keepAllOnTop();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Scry reorder decides which card is revealed")
    void scryReorderDecidesRevealedCard() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears, new Forest(), new Forest(),
                new Forest(), new Forest()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();
        // Put the Forest on the bottom so the Grizzly Bears is revealed instead.
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 2, 3, 4), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }
}
