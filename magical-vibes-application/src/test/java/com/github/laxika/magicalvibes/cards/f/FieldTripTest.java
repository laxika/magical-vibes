package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BayouGroff;
import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FieldTrip.class, Forest.class, Plains.class, Island.class, BayouGroff.class,
        EnvironmentalSciences.class})
class FieldTripTest extends BaseCardTest {

    @Test
    @DisplayName("Searches only for a basic Forest and puts it onto the battlefield tapped")
    void searchesForBasicForestOntoBattlefieldTapped() {
        Forest forest = new Forest();
        castFieldTrip(List.of(forest, new Plains(), new Island(), new BayouGroff()));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest).hasSize(3);
        harness.assertInGraveyard(player1, "Field Trip");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Learn can discard a card and draw after the land search")
    void learnsByDiscardingAndDrawing() {
        Card discarded = new BayouGroff();
        Card drawn = new Island();
        harness.setHand(player1, List.of(new FieldTrip(), discarded));
        harness.setLibrary(player1, List.of(new Forest(), drawn));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Bayou Groff");
        harness.assertInHand(player1, "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void learnsByTakingOnlyAnOwnedLessonAfterSearching() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new BayouGroff();
        Card opponentsLesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(opponentsLesson)));
        castFieldTrip(List.of(new Forest()));

        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        assertThat(search.params().reveals()).isTrue();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(opponentsLesson);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canDeclineBothLearnOptions() {
        Card kept = new BayouGroff();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new FieldTrip(), kept));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void failingToFindStillLearns() {
        Card forest = new Forest();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        castFieldTrip(List.of(forest));

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void libraryWithoutForestsStillLearnsWithoutOfferingOtherBasicLands() {
        Card island = new Island();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        castFieldTrip(List.of(island));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
    }

    @Test
    void emptyLibraryStillAllowsTakingALesson() {
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        castFieldTrip(List.of());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Field Trip");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castFieldTrip(List<Card> library) {
        harness.setHand(player1, List.of(new FieldTrip()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
