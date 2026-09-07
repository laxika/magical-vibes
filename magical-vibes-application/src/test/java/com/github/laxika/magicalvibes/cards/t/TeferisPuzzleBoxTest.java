package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.j.JamuraanLion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeferisPuzzleBox.class, JamuraanLion.class, Impulse.class})
class TeferisPuzzleBoxTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        gd.turnNumber = 2; // avoid the starting player's first-turn draw skip
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities(); // advances from UPKEEP to DRAW (fires the normal draw + trigger)
    }

    private List<Card> impulses(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Impulse());
        }
        return cards;
    }

    @Test
    @DisplayName("Active player cycles their hand into the bottom of their library and draws that many")
    void activePlayerCyclesHand() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card handMarker = new JamuraanLion();
        List<Card> library = impulses(5);
        harness.setHand(player1, List.of(handMarker));
        harness.setLibrary(player1, library); // enough to survive the normal draw + re-draw

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the Puzzle Box trigger
        chooseCurrentOrder(player1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(handMarker);
        assertThat(gd.playerDecks.get(player1.getId())).contains(handMarker);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2));
    }

    @Test
    @DisplayName("Triggers on an opponent's draw step and cycles that player's hand")
    void triggersOnOpponentDrawStep() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card handMarker = new JamuraanLion();
        List<Card> library = impulses(5);
        harness.setHand(player2, List.of(handMarker));
        harness.setLibrary(player2, library);

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve the Puzzle Box trigger
        chooseCurrentOrder(player2);

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(handMarker);
        assertThat(gd.playerDecks.get(player2.getId())).contains(handMarker);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2));
    }

    @Test
    @DisplayName("Cards put on the bottom are drawable again — same count returns to hand")
    void handSizeIsPreserved() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card firstHandCard = new JamuraanLion();
        Card secondHandCard = new JamuraanLion();
        List<Card> library = impulses(6);
        harness.setHand(player1, List.of(firstHandCard, secondHandCard));
        harness.setLibrary(player1, library);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the Puzzle Box trigger
        chooseCurrentOrder(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2), library.get(3));
    }

    @Test
    @DisplayName("Lets the player choose the order of cards put on the bottom of their library")
    void choosesOrderForCardsPutOnBottom() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card firstHandCard = new JamuraanLion();
        Card secondHandCard = new TeferisPuzzleBox();
        Card normalDraw = new Impulse();
        Card remainingLibraryCard = new Impulse();
        harness.setHand(player1, List.of(firstHandCard, secondHandCard));
        harness.setLibrary(player1, List.of(normalDraw, remainingLibraryCard));

        advanceToDraw(player1);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
        assertThat(reorder.cards()).containsExactlyInAnyOrder(firstHandCard, secondHandCard, normalDraw);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(
                reorder.cards().indexOf(normalDraw),
                reorder.cards().indexOf(secondHandCard),
                reorder.cards().indexOf(firstHandCard))));

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(remainingLibraryCard, normalDraw, secondHandCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstHandCard);
    }

    private void chooseCurrentOrder(Player player) {
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player,
                new InteractionAnswer.CardOrder(java.util.stream.IntStream.range(0, reorder.cards().size())
                        .boxed().toList()));
    }
}
