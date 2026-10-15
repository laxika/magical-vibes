package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
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

@CardUsed({ProfessorOfSymbology.class, EnvironmentalSciences.class, EagerFirstYear.class})
class ProfessorOfSymbologyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB Learn reveals a Lesson after declining to discard")
    void etbLearnSearchesForLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        castProfessor(new EagerFirstYear());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("ETB Learn discards and draws when the discard branch is accepted")
    void etbLearnDiscardsAndDraws() {
        Card discarded = new EagerFirstYear();
        Card drawn = new EagerFirstYear();
        harness.setLibrary(player1, List.of(drawn));

        castProfessor(discarded);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("ETB Learn searches directly for a Lesson when the hand is empty")
    void etbLearnSearchesWithEmptyHand() {
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castProfessor();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }

    @Test
    void mayDeclineBothDiscardAndLesson() {
        Card lesson = new EnvironmentalSciences();
        Card kept = new EagerFirstYear();
        Card undrawn = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setLibrary(player1, List.of(undrawn));

        castProfessor(kept);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void discardChoiceDoesNotAlsoTakeAvailableLesson() {
        Card lesson = new EnvironmentalSciences();
        Card discarded = new EagerFirstYear();
        Card drawn = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setLibrary(player1, List.of(drawn));

        castProfessor(discarded);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyHandAndNoOwnedLessonDoesNothing() {
        Card nonLesson = new EagerFirstYear();
        Card opponentsLesson = new EnvironmentalSciences();
        Card undrawn = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(nonLesson)));
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(opponentsLesson)));
        harness.setLibrary(player1, List.of(undrawn));

        castProfessor();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(opponentsLesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castProfessor(Card... additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new ProfessorOfSymbology());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
