package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WortTheRaidmother;
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

@CardUsed({InterdisciplinaryStudies.class, EnvironmentalSciences.class, GrizzlyBears.class,
        WortTheRaidmother.class})
class InterdisciplinaryStudiesTest extends BaseCardTest {

    @Test
    @DisplayName("Seeks a multicolored card, then learns")
    void seeksMulticoloredCardThenLearns() {
        Card multicolored = new WortTheRaidmother();
        Card monocolored = new GrizzlyBears();
        Card lesson = new EnvironmentalSciences();
        harness.setLibrary(player1, List.of(monocolored, multicolored));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new InterdisciplinaryStudies()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(multicolored);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(monocolored);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }

    @Test
    @DisplayName("The sought card can be discarded to learn without shuffling the remaining library")
    void discardsSoughtCardToDrawTopCard() {
        Card multicolored = new WortTheRaidmother();
        Card top = new GrizzlyBears();
        Card bottom = new EnvironmentalSciences();
        Card lesson = new EnvironmentalSciences();
        harness.setLibrary(player1, List.of(top, multicolored, bottom));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castStudies();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(multicolored);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(multicolored);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Learn still fetches a Lesson when seek finds no multicolored card")
    void learnsWhenSeekFindsNothing() {
        Card monocolored = new GrizzlyBears();
        Card colorless = new EnvironmentalSciences();
        Card lesson = new EnvironmentalSciences();
        harness.setLibrary(player1, List.of(monocolored, colorless));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, new GrizzlyBears())));

        castStudies();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(monocolored, colorless);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).doesNotContain(lesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Both optional learn actions can be declined")
    void declinesDiscardAndLesson() {
        Card multicolored = new WortTheRaidmother();
        Card lesson = new EnvironmentalSciences();
        harness.setLibrary(player1, List.of(multicolored));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castStudies();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(multicolored);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library and no Lessons do not prevent the spell resolving")
    void resolvesWithEmptyLibraryAndNoLessons() {
        harness.setLibrary(player1, List.of());
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());

        castStudies();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(InterdisciplinaryStudies.class::isInstance);
    }

    private void castStudies() {
        harness.setHand(player1, List.of(new InterdisciplinaryStudies()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
