package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PopQuizTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, then learns by searching for a Lesson")
    void drawsAndSearchesForLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new GrizzlyBears();
        Card drawn = new Forest();
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

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn, lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("Draws a card, then learns by discarding and drawing")
    void drawsAndDiscardsToDraw() {
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(new PopQuiz(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    private void castPopQuiz() {
        harness.setHand(player1, List.of(new PopQuiz()));
        addMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
