package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Addle;
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

@CardUsed({ScoutingTrek.class, Addle.class, Forest.class, Plains.class})
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

    private void cast() {
        harness.castFromHand(player1, new ScoutingTrek(), "{1}{G}");
        harness.passBothPriorities();
    }
}
