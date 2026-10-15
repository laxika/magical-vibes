package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HymnToTheAges.class})
class HymnToTheAgesTest extends BaseCardTest {

    @Test
    void drawsItsStartingIntensityAndIntensifiesOwnedChorusCards() {
        HymnToTheAges hymn = new HymnToTheAges();
        HymnToTheAges otherHymn = new HymnToTheAges();
        HymnToTheAges drawnHymn = new HymnToTheAges();
        harness.setLibrary(player1, List.of(drawnHymn, otherHymn));

        harness.castFromHand(player1, hymn, "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .first()
                .isSameAs(drawnHymn);
        assertThat(gd.getCardIntensity(hymn.getId())).isEqualTo(2);
        assertThat(gd.getCardIntensity(otherHymn.getId())).isEqualTo(2);
        assertThat(gd.getCardIntensity(drawnHymn.getId())).isEqualTo(2);
    }

    @Test
    void subsequentHymnDrawsTwoCardsAfterBeingIntensifiedInLibrary() {
        HymnToTheAges first = new HymnToTheAges();
        HymnToTheAges second = new HymnToTheAges();
        HymnToTheAges remaining = new HymnToTheAges();
        harness.setLibrary(player1, List.of(second, new HymnToTheAges(), new HymnToTheAges(), remaining));
        harness.castFromHand(player1, first, "{1}{U}");
        harness.passBothPriorities();

        harness.castFromHand(player1, second, "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getCardIntensity(first.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(second.getId())).isEqualTo(3);
    }

    @Test
    void intensifiesOwnedCardsInGraveyardAndExileButNotOpponentsCards() {
        HymnToTheAges graveyardHymn = new HymnToTheAges();
        HymnToTheAges exiledHymn = new HymnToTheAges();
        HymnToTheAges opponentsHymn = new HymnToTheAges();
        harness.setGraveyard(player1, List.of(graveyardHymn));
        harness.setExile(player1, List.of(exiledHymn));
        harness.setHand(player2, List.of(opponentsHymn));
        harness.setLibrary(player1, List.of(new HymnToTheAges(), new HymnToTheAges()));

        harness.castFromHand(player1, new HymnToTheAges(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(graveyardHymn)).isEqualTo(2);
        assertThat(gd.getCardIntensity(exiledHymn)).isEqualTo(2);
        assertThat(gd.getCardIntensity(opponentsHymn)).isEqualTo(1);
    }
}
