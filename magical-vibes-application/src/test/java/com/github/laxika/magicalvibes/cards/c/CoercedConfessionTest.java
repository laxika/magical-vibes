package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoercedConfessionTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills four cards and controller draws one per creature milled")
    void millsFourAndDrawsPerCreature() {
        prepare();
        setDeck(player2, new GrizzlyBears(), new Mountain(), new GrizzlyBears(), new Mountain(),
                new Mountain());
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        // The spell itself leaves the hand, so the draws are measured against the post-cast size.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1 + 2);
    }

    @Test
    @DisplayName("No draws when no creature cards are milled")
    void noDrawsWithoutCreatures() {
        prepare();
        setDeck(player2, new Mountain(), new Mountain(), new Mountain(), new Mountain());
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1);
    }

    @Test
    @DisplayName("Can target yourself, milling and drawing from your own library")
    void canTargetController() {
        prepare();
        setDeck(player1, new GrizzlyBears(), new GrizzlyBears(), new Mountain(), new Mountain(),
                new Mountain(), new Mountain());
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Four milled, two of them creatures, then two of the remaining cards drawn.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(5); // 4 milled + Coerced Confession
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1 + 2);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library is smaller than four")
    void millsOnlyRemainingWhenLibrarySmall() {
        prepare();
        setDeck(player2, new GrizzlyBears(), new Mountain());
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1 + 1);
    }

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CoercedConfession()));
        harness.addMana(player1, ManaColor.BLUE, 6);
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
