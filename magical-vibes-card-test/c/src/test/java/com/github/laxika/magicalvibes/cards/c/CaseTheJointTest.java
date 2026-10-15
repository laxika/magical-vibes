package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaseTheJoint.class, Forest.class, Mountain.class})
class CaseTheJointTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards before privately looking at the top card of every library")
    void drawsThenLooksAtEachLibraryTop() {
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        Card ownTop = new Forest();
        Card opponentTop = new Mountain();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, ownTop));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.castFromHand(player1, new CaseTheJoint(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .anySatisfy(message -> assertThat(message).contains("Forest"))
                .anySatisfy(message -> assertThat(message).contains("Mountain"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
    }

    @Test
    @DisplayName("Empty libraries are skipped after the draw")
    void skipsEmptyLibraries() {
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, new CaseTheJoint(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Still looks at the opponent's library when drawing empties the caster's library")
    void looksAtOpponentTopAfterDrawingLastTwoCards() {
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        Card opponentTop = new Mountain();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(opponentTop));

        harness.castFromHand(player1, new CaseTheJoint(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .hasSize(1)
                .allSatisfy(message -> assertThat(message).contains("Mountain"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
    }

    @Test
    @DisplayName("Player two draws and privately looks even when player one's library is empty")
    void secondPlayerLooksWithEmptyOpposingLibrary() {
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        Card ownTop = new Forest();
        Card nextCard = new Mountain();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(firstDraw, secondDraw, ownTop, nextCard));

        harness.castFromHand(player2, new CaseTheJoint(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(ownTop, nextCard);
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .hasSize(1)
                .allSatisfy(message -> assertThat(message).contains("Forest").doesNotContain("Mountain"));
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
    }
}
