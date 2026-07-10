package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeferisPuzzleBoxTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid the starting player's first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW (fires the normal draw + trigger)
    }

    private List<Card> plains(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Plains());
        }
        return cards;
    }

    @Test
    @DisplayName("Active player cycles their hand into the bottom of their library and draws that many")
    void activePlayerCyclesHand() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card handMarker = new GrizzlyBears();
        harness.setHand(player1, List.of(handMarker));
        harness.setLibrary(player1, plains(5)); // enough to survive the normal draw + re-draw

        advanceToDraw(player1);
        // Normal draw already pulled a Plains into hand → hand is [handMarker, Plains].
        harness.passBothPriorities(); // resolve the Puzzle Box trigger

        // The old hand (marker + drawn Plains) is cycled to the bottom of the library.
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(handMarker);
        assertThat(gd.playerDecks.get(player1.getId())).contains(handMarker);

        // Player drew that many fresh cards — hand size is preserved (2 cards: marker + normal draw).
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Triggers on an opponent's draw step and cycles that player's hand")
    void triggersOnOpponentDrawStep() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card handMarker = new GrizzlyBears();
        harness.setHand(player2, List.of(handMarker));
        harness.setLibrary(player2, plains(5));

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve the Puzzle Box trigger

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(handMarker);
        assertThat(gd.playerDecks.get(player2.getId())).contains(handMarker);
        assertThat(gd.playerHands.get(player2.getId())).allMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Cards put on the bottom are drawable again — same count returns to hand")
    void handSizeIsPreserved() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, plains(6));

        advanceToDraw(player1);
        // After the normal draw hand is 3 (2 bears + 1 Plains).
        int handAfterNormalDraw = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve the Puzzle Box trigger

        // Hand is fully replaced with the same number of freshly drawn cards.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handAfterNormalDraw);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(c -> c.getName().equals("Plains"));
    }
}
