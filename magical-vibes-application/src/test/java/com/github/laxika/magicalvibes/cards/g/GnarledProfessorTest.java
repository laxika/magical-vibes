package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GnarledProfessorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB Learn reveals a Lesson after declining to discard")
    void etbLearnSearchesForLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        castProfessor(new Forest());

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
        Card discarded = new Forest();
        Card drawn = new Forest();
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

    private void castProfessor(Card... additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new GnarledProfessor());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
