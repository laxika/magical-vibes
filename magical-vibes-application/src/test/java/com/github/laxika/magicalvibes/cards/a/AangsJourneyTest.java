package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HondenOfLifesWeb;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AangsJourney.class, Forest.class, GrizzlyBears.class, HondenOfLifesWeb.class})
class AangsJourneyTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, searches for a basic land and gains 2 life")
    void withoutKickerSearchesForBasicLandAndGainsLife() {
        Forest forest = new Forest();
        cast(false, forest, new HondenOfLifesWeb());

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("When kicked, searches for a basic land and then a Shrine, then gains 2 life")
    void kickedSearchesForBasicLandAndShrineAndGainsLife() {
        Forest forest = new Forest();
        HondenOfLifesWeb shrine = new HondenOfLifesWeb();
        cast(true, forest, shrine);

        PendingInteraction.LibrarySearch basicLandSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(basicLandSearch).isNotNull();
        assertThat(basicLandSearch.params().cards()).containsExactly(forest);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch shrineSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(shrineSearch).isNotNull();
        assertThat(shrineSearch.params().cards()).containsExactly(shrine);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, shrine);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    private void cast(boolean kicked, Forest forest, HondenOfLifesWeb shrine) {
        harness.setHand(player1, List.of(new AangsJourney()));
        harness.addMana(player1, ManaColor.COLORLESS, kicked ? 4 : 2);
        harness.setLibrary(player1, List.of(forest, shrine, new GrizzlyBears()));

        if (kicked) {
            harness.castKickedSorcery(player1, 0, null);
        } else {
            harness.castSorcery(player1, 0, 0);
        }
        harness.passBothPriorities();
    }
}
