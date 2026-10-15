package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PopQuiz.class, EnvironmentalSciences.class, EagerFirstYear.class})
class PopQuizTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, then learns by searching for a Lesson")
    void drawsAndSearchesForLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new EagerFirstYear();
        Card drawn = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));
        harness.setLibrary(player1, List.of(drawn));

        castPopQuiz();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn, lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Draws a card, then learns by discarding and drawing")
    void drawsAndDiscardsToDraw() {
        Card discarded = new EagerFirstYear();
        Card drawn = new EagerFirstYear();
        Card learnedDraw = new EagerFirstYear();
        harness.setHand(player1, List.of(new PopQuiz(), discarded));
        harness.setLibrary(player1, List.of(drawn, learnedDraw));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded, drawn);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn, learnedDraw);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("May decline both learn options even with a Lesson available")
    void mayDeclineLearning() {
        Card lesson = new EnvironmentalSciences();
        Card drawn = new EagerFirstYear();
        Card remaining = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setLibrary(player1, List.of(drawn, remaining));

        castPopQuiz();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(EagerFirstYear.class::isInstance);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The card drawn by Pop Quiz can be discarded to learn")
    void mayDiscardTheCardJustDrawn() {
        Card drawn = new EagerFirstYear();
        Card learnedDraw = new EagerFirstYear();
        harness.setLibrary(player1, List.of(drawn, learnedDraw));

        castPopQuiz();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(learnedDraw);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining to discard with no own Lesson finishes without taking an opponent's Lesson")
    void noLessonAvailableAfterDecliningDiscard() {
        Card nonLesson = new EagerFirstYear();
        Card opponentLesson = new EnvironmentalSciences();
        Card drawn = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(nonLesson)));
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(opponentLesson)));
        harness.setLibrary(player1, List.of(drawn));

        castPopQuiz();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(opponentLesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castPopQuiz() {
        harness.castFromHand(player1, new PopQuiz(), "{2}{U}");
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
