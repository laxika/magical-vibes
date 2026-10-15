package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EyeblightsEnding;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ringskipper.class, EyeblightsEnding.class, Forest.class, Ponder.class})
class RingskipperTest extends BaseCardTest {

    private Card killRingskipper() {
        Card card = killRingskipperAndStartClash();
        completePlacements(false);
        return card;
    }

    private void completePlacements(boolean bottom) {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry scry) {
            gs.handleInteractionAnswer(gd,
                    scry.playerId().equals(player1.getId()) ? player1 : player2,
                    new InteractionAnswer.ScryOrder(bottom ? List.of() : List.of(0),
                            bottom ? List.of(0) : List.of()));
        }
    }

    private Card killRingskipperAndStartClash() {
        Permanent ringskipper = harness.addToBattlefieldAndReturn(player1, new Ringskipper());
        Card ringskipperCard = ringskipper.getCard();

        // Player 2 destroys Ringskipper — it dies, ON_DEATH clash trigger goes on the stack.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new EyeblightsEnding()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, ringskipper.getId());
        harness.passBothPriorities(); // resolve the death clash effect

        return ringskipperCard;
    }


    @Test
    @DisplayName("The active opponent chooses placement before the nonactive trigger controller")
    void activeOpponentChoosesFirst() {
        harness.setLibrary(player1, List.of(new Ponder()));
        harness.setLibrary(player2, List.of(new Forest()));

        killRingskipperAndStartClash();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Clash cards move only after both players choose their placement")
    void placementsMoveSimultaneously() {
        Ponder revealed = new Ponder();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(new Forest()));
        killRingskipperAndStartClash();
        PendingInteraction.Scry first = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd,
                first.playerId().equals(player1.getId()) ? player1 : player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed, next);
    }

    @Test
    @DisplayName("An empty controller library cannot win even against an empty opponent library")
    void bothLibrariesEmptyLeavesCardInGraveyard() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        Card card = killRingskipper();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Bottoming the revealed cards preserves the clash win")
    void bottomingCardsPreservesWin() {
        Ponder revealed = new Ponder();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(new Forest()));

        Card card = killRingskipperAndStartClash();
        completePlacements(true);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, revealed);
    }

    @Test
    @DisplayName("Winning the clash returns Ringskipper to its owner's hand")
    void wonClashReturnsToHand() {
        // Higher mana value on top for player1 (Ponder MV 1 > Forest MV 0) → player1 wins.
        harness.setLibrary(player1, List.of(new Ponder()));
        harness.setLibrary(player2, List.of(new Forest()));

        Card ringskipperCard = killRingskipper();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ringskipperCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(ringskipperCard.getId()));
    }

    @Test
    @DisplayName("A clash against an empty opponent library still returns Ringskipper on a win")
    void wonClashAgainstEmptyOpponentLibraryReturnsToHand() {
        harness.setLibrary(player1, List.of(new Ponder()));
        harness.setLibrary(player2, List.of());

        Card ringskipperCard = killRingskipper();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ringskipperCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(ringskipperCard.getId()));
    }


    @Test
    @DisplayName("Losing the clash leaves Ringskipper in the graveyard")
    void lostClashStaysInGraveyard() {
        // Lower mana value on top for player1 (Forest MV 0 < Ponder MV 1) → player1 loses.
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Ponder()));

        Card ringskipperCard = killRingskipper();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ringskipperCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(ringskipperCard.getId()));
    }


    @Test
    @DisplayName("An equal mana value tie is not a win, so Ringskipper stays in the graveyard")
    void tiedClashStaysInGraveyard() {
        // Equal mana values (both Ponder MV 1) → no one wins the clash.
        harness.setLibrary(player1, List.of(new Ponder()));
        harness.setLibrary(player2, List.of(new Ponder()));

        Card ringskipperCard = killRingskipper();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ringskipperCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(ringskipperCard.getId()));
    }
}
