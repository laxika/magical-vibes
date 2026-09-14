package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.j.JamuraanLion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, Impulse.class, JamuraanLion.class, TeferisPuzzleBox.class})
class TeferisPuzzleBoxTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        gd.turnNumber = 2; // avoid the starting player's first-turn draw skip
        advanceToUpkeep(activePlayer);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("Active player cycles their hand into the bottom of their library and draws that many")
    void activePlayerCyclesHand() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card handMarker = new GrizzlyBears();
        List<Card> library = libraryCards(5);
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

        Card handMarker = new GrizzlyBears();
        List<Card> library = libraryCards(5);
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

        Card firstHandCard = new GrizzlyBears();
        Card secondHandCard = new GrizzlyBears();
        List<Card> library = libraryCards(6);
        harness.setHand(player1, List.of(firstHandCard, secondHandCard));
        harness.setLibrary(player1, library);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the Puzzle Box trigger
        chooseCurrentOrder(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2), library.get(3));
    }

    @Test
    @DisplayName("Exchanges the single card drawn when the hand was initially empty")
    void exchangesSingleCardAfterNormalDraw() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card firstLibraryCard = new Impulse();
        Card secondLibraryCard = new Impulse();
        Card thirdLibraryCard = new Impulse();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstLibraryCard, secondLibraryCard, thirdLibraryCard));

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the Puzzle Box trigger without a reorder prompt

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondLibraryCard);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(thirdLibraryCard, firstLibraryCard);
    }

    @Test
    @DisplayName("Lets the player choose the order of cards put on the bottom of their library")
    void choosesOrderForCardsPutOnBottom() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card firstHandCard = new GrizzlyBears();
        Card secondHandCard = new TeferisPuzzleBox();
        Card normalDraw = new GrizzlyBears();
        Card remainingLibraryCard = new GrizzlyBears();
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

    @Test
    @DisplayName("Does not trigger during the starting player's skipped first draw step")
    void doesNotTriggerOnStartingPlayersFirstDrawStep() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card handCard = new JamuraanLion();
        Card topLibraryCard = new Impulse();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(topLibraryCard));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 1;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topLibraryCard);
        assertThat(gd.stack).isEmpty();
    }

    private void chooseCurrentOrder(Player player) {
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player,
                new InteractionAnswer.CardOrder(java.util.stream.IntStream.range(0, reorder.cards().size())
                        .boxed().toList()));
    }

    private List<Card> libraryCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    @Test
    @DisplayName("Each Puzzle Box creates an independent draw-step trigger")
    void eachPuzzleBoxTriggersIndependently() {
        harness.addToBattlefield(player1, new TeferisPuzzleBox());
        harness.addToBattlefield(player1, new TeferisPuzzleBox());

        Card handMarker = new GrizzlyBears();
        List<Card> library = libraryCards(5);
        harness.setHand(player1, List.of(handMarker));
        harness.setLibrary(player1, library);

        advanceToDraw(player1);
        harness.passBothPriorities();
        chooseCurrentOrder(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2));

        harness.passBothPriorities();
        chooseCurrentOrder(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(3), library.get(4));
    }

    @Test
    @DisplayName("A draw-step trigger still resolves after the Puzzle Box leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        var puzzleBox = harness.addToBattlefieldAndReturn(player1, new TeferisPuzzleBox());
        Card handMarker = new GrizzlyBears();
        List<Card> library = libraryCards(5);
        harness.setHand(player1, List.of(handMarker));
        harness.setLibrary(player1, library);

        advanceToDraw(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, puzzleBox));
        harness.passBothPriorities();
        chooseCurrentOrder(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2));
    }
}
