package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Addle;
import com.github.laxika.magicalvibes.cards.e.ElfhamePalace;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScoutingTrek.class, Addle.class, ElfhamePalace.class, Forest.class, Plains.class})
class ScoutingTrekTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers any number of basic land cards and no other cards")
    void offersBasicLandsOnly() {
        Card plains = new Plains();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(new Addle(), plains, new Addle(), forest));

        cast();

        PendingInteraction.SearchLibraryToTopChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.pool()).containsExactlyInAnyOrder(plains, forest);
    }

    @Test
    @DisplayName("Choosing multiple basic lands puts them on top in the chosen order")
    void choosingMultipleBasicLandsPutsThemOnTop() {
        Card plains = new Plains();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(plains, new Addle(), forest));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(plains.getId(), forest.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(forest);
        assertThat(gd.playerDecks.get(player1.getId()).get(1)).isSameAs(plains);
    }

    @Test
    @DisplayName("Choosing a subset reveals it and returns unchosen cards to the library")
    void choosingSubsetRevealsAndReturnsUnchosenCards() {
        Card chosen = new Plains();
        Card unchosen = new Forest();
        Card nonland = new Addle();
        harness.setLibrary(player1, List.of(chosen, nonland, unchosen));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(3)
                .contains(unchosen, nonland);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
        assertThat(gameLogContains("reveals " + chosen.getName())).isTrue();
    }

    @Test
    @DisplayName("Choosing no basic lands leaves the library intact")
    void choosingNoBasicLandsLeavesLibraryIntact() {
        Card forest = new Forest();
        Card nonland = new Addle();
        harness.setLibrary(player1, List.of(nonland, forest));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonland, forest);
    }

    @Test
    @DisplayName("No basic lands in the library does not prompt")
    void noBasicLandsDoesNotPrompt() {
        harness.setLibrary(player1, List.of(new Addle()));

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class)).isNull();
    }

    @Test
    @DisplayName("Nonbasic lands are excluded and the opponent's library is untouched")
    void excludesNonbasicLandsAndSearchesOnlyControllersLibrary() {
        Card basic = new Forest();
        Card nonbasic = new ElfhamePalace();
        Card opponentsLand = new Plains();
        harness.setLibrary(player1, List.of(nonbasic, basic));
        harness.setLibrary(player2, List.of(opponentsLand));

        cast();

        PendingInteraction.SearchLibraryToTopChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class);
        assertThat(choice.pool()).containsExactly(basic);
        harness.handleMultipleCardsChosen(player1, List.of(basic.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basic, nonbasic);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library finishes resolution without a choice")
    void emptyLibraryFinishesResolution() {
        harness.setLibrary(player1, List.of());

        cast();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("All basic lands may be chosen, including multiple copies of the same name")
    void choosesAllBasicLandsIncludingSameNamedCopies() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Plains();
        harness.setLibrary(player1, List.of(first, second, third));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals " + first.getName() + ", " + second.getName()
                + ", " + third.getName())).isTrue();
    }

    private void cast() {
        harness.castFromHand(player1, new ScoutingTrek(), "{1}{G}");
        harness.passBothPriorities();
    }
}
