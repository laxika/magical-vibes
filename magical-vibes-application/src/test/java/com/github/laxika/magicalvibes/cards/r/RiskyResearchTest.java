package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiskyResearch.class, GrizzlyBears.class})
class RiskyResearchTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils two, then draws two cards and loses 2 life")
    void surveilsDrawsAndLosesLife() {
        Card surveiledCardOne = new GrizzlyBears();
        Card surveiledCardTwo = new GrizzlyBears();
        Card drawnCardOne = new GrizzlyBears();
        Card drawnCardTwo = new GrizzlyBears();
        harness.setLibrary(player1, List.of(surveiledCardOne, surveiledCardTwo, drawnCardOne, drawnCardTwo));
        harness.setHand(player1, List.of(new RiskyResearch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveiledCardOne, surveiledCardTwo);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCardOne, drawnCardTwo);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Waits for surveil, then draws the kept cards in their chosen order")
    void drawsReorderedKeptCards() {
        Card first = new RiskyResearch();
        Card second = new RiskyResearch();
        Card remaining = new RiskyResearch();
        Card spell = new RiskyResearch();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Draws the kept card and the next library card when surveil puts one in the graveyard")
    void splitsSurveilledCardsBetweenLibraryAndGraveyard() {
        Card rejected = new RiskyResearch();
        Card kept = new RiskyResearch();
        Card next = new RiskyResearch();
        Card remaining = new RiskyResearch();
        Card spell = new RiskyResearch();
        harness.setLibrary(player1, List.of(rejected, kept, next, remaining));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, next);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(rejected, spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }
}
