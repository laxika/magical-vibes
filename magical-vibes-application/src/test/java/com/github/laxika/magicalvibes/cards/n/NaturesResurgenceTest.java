package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GaeasBlessing;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaeasBlessing.class, NaturesResurgence.class, RedwoodTreefolk.class})
class NaturesResurgenceTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws a card for each creature card in their own graveyard")
    void eachPlayerDrawsForOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new RedwoodTreefolk(), new RedwoodTreefolk()));

        harness.setGraveyard(player2, List.of(
                new RedwoodTreefolk(), new RedwoodTreefolk(), new RedwoodTreefolk()));

        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new NaturesResurgence(), "{2}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 3);
    }

    @Test
    @DisplayName("Only creature cards count; non-creature cards are ignored")
    void onlyCreatureCardsCount() {
        harness.setGraveyard(player1, List.of(new RedwoodTreefolk(), new GaeasBlessing(), new GaeasBlessing()));

        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new NaturesResurgence(), "{2}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 1);
    }

    @Test
    @DisplayName("A player with no creature cards in their graveyard draws nothing")
    void playerWithoutCreatureCardsDrawsNothing() {
        harness.setGraveyard(player1, List.of(new RedwoodTreefolk()));
        harness.setGraveyard(player2, List.of(new GaeasBlessing()));

        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new NaturesResurgence(), "{2}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore);
    }

    @Test
    @DisplayName("Both players draw nothing when both graveyards are empty")
    void emptyGraveyardsDrawNothing() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of());
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, new NaturesResurgence(), "{2}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore);
        harness.assertInGraveyard(player1, "Nature's Resurgence");
    }

    @Test
    @DisplayName("Creature counts are determined at resolution rather than when cast")
    void countsGraveyardsAtResolution() {
        harness.setGraveyard(player1, List.of(new RedwoodTreefolk()));
        harness.setGraveyard(player2, List.of(new RedwoodTreefolk(), new RedwoodTreefolk()));
        harness.setHand(player2, List.of());
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, new NaturesResurgence(), "{2}{G}{G}");
        harness.setGraveyard(player1, List.of(new RedwoodTreefolk(), new RedwoodTreefolk()));
        harness.setGraveyard(player2, List.of(new GaeasBlessing()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore);
    }
}
