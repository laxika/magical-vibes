package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.s.ScurridColony;
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

@CardUsed({CramSession.class, EnvironmentalSciences.class, ScurridColony.class})
class CramSessionTest extends BaseCardTest {

    @Test
    @DisplayName("Gains four life and searches the sideboard for a Lesson")
    void gainsLifeAndFindsLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new ScurridColony();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        castCramSession();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("Gains four life and can discard a card to draw a card")
    void gainsLifeAndDiscardsToDraw() {
        Card discarded = new ScurridColony();
        Card drawn = new EnvironmentalSciences();
        harness.setHand(player1, List.of(new CramSession(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Can decline both Learn choices while still gaining life")
    void canDeclineBothLearnChoices() {
        Card kept = new ScurridColony();
        Card lesson = new EnvironmentalSciences();
        Card libraryCard = new ScurridColony();
        harness.setHand(player1, List.of(new CramSession(), kept));
        harness.setLibrary(player1, List.of(libraryCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Learning with an empty hand and no Lessons neither discards nor draws")
    void emptyHandAndNoLessonsOnlyGainsLife() {
        Card nonLesson = new ScurridColony();
        Card libraryCard = new EnvironmentalSciences();
        harness.setLibrary(player1, List.of(libraryCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(nonLesson)));

        castCramSession();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Discarding to draw does not also take an available Lesson")
    void discardChoiceDoesNotTakeLesson() {
        Card discarded = new ScurridColony();
        Card drawn = new ScurridColony();
        Card lesson = new EnvironmentalSciences();
        harness.setHand(player1, List.of(new CramSession(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 24);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining discard allows taking only your own Lesson without drawing")
    void declinesDiscardAndTakesOwnLesson() {
        Card kept = new ScurridColony();
        Card lesson = new EnvironmentalSciences();
        Card opponentsLesson = new EnvironmentalSciences();
        Card libraryCard = new ScurridColony();
        harness.setHand(player1, List.of(new CramSession(), kept));
        harness.setLibrary(player1, List.of(libraryCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(opponentsLesson)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, lesson);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(opponentsLesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castCramSession() {
        harness.setHand(player1, List.of(new CramSession()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
