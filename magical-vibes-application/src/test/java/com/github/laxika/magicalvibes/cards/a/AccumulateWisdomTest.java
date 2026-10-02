package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GranGran;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AccumulateWisdom.class, AirbendingLesson.class, GranGran.class})
class AccumulateWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top three cards into hand and the rest on the bottom")
    void choosesOneOfTopThreeAndOrdersRest() {
        Card top1 = new GranGran();
        Card top2 = new GranGran();
        Card top3 = new GranGran();
        harness.setLibrary(player1, List.of(top1, top2, top3));
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(top2).doesNotContain(top1, top3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top3, top1);
    }

    @Test
    @DisplayName("Puts all three cards into hand with three Lessons in the graveyard")
    void putsAllThreeIntoHandWithThreeLessons() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        Card top1 = new GranGran();
        Card top2 = new GranGran();
        Card top3 = new GranGran();
        harness.setLibrary(player1, List.of(top1, top2, top3));
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(top1, top2, top3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Two Lessons plus non-Lessons and opposing Lessons do not enable the replacement")
    void countsOnlyLessonsInControllersGraveyard() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AccumulateWisdom(), new GranGran()));
        harness.setGraveyard(player2, List.of(new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        Card top1 = new GranGran();
        Card top2 = new AirbendingLesson();
        Card top3 = new AccumulateWisdom();
        Card untouched = new GranGran();
        harness.setLibrary(player1, List.of(top1, top2, top3, untouched));
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, top3, top1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "0, 1", "3, 0", "3, 1", "3, 2", "4, 4"})
    @DisplayName("Handles short libraries and takes no more than the top three with the replacement")
    void handlesLibrarySize(int lessonCount, int librarySize) {
        List<Card> lessons = IntStream.range(0, lessonCount)
                .mapToObj(i -> (Card) new AirbendingLesson()).toList();
        List<Card> library = IntStream.range(0, librarySize)
                .mapToObj(i -> (Card) new GranGran()).toList();
        harness.setGraveyard(player1, lessons);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        int taken = Math.min(3, librarySize);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library.subList(0, taken));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library.subList(taken, librarySize));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(lessonCount + 1);
    }

    @Test
    @DisplayName("With two cards and no Lessons, takes one and puts the other on the bottom")
    void choosesOneFromTwoCards() {
        Card top1 = new GranGran();
        Card top2 = new AirbendingLesson();
        harness.setLibrary(player1, List.of(top1, top2));
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Checks the Lesson threshold on resolution rather than on casting")
    void checksThresholdOnResolution() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson()));
        Card top1 = new GranGran();
        Card top2 = new AirbendingLesson();
        Card top3 = new AccumulateWisdom();
        harness.setLibrary(player1, List.of(top1, top2, top3));
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        addMana();

        harness.castInstant(player1, 0);
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson(), new AccumulateWisdom()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top1, top2, top3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Losing the third Lesson before resolution restores the one-card choice")
    void losingLessonBeforeResolutionDisablesReplacement() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson(), new AccumulateWisdom()));
        Card top1 = new GranGran();
        Card top2 = new AirbendingLesson();
        Card top3 = new AccumulateWisdom();
        harness.setLibrary(player1, List.of(top1, top2, top3));
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        addMana();

        harness.castInstant(player1, 0);
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top2, top3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
