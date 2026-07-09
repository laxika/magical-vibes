package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RingskipperTest extends BaseCardTest {

    private Card killRingskipper() {
        harness.addToBattlefield(player1, new Ringskipper());
        Permanent ringskipper = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card ringskipperCard = ringskipper.getCard();

        // Player 2 destroys Ringskipper — it dies, ON_DEATH clash trigger goes on the stack.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, ringskipper.getId());
        harness.passBothPriorities(); // resolve Doom Blade — Ringskipper dies, death trigger placed
        harness.passBothPriorities(); // resolve the death clash effect

        return ringskipperCard;
    }

    // ===== Won clash — return this card to its owner's hand =====

    @Test
    @DisplayName("Winning the clash returns Ringskipper to its owner's hand")
    void wonClashReturnsToHand() {
        // Higher mana value on top for player1 (Grizzly Bears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        Card ringskipperCard = killRingskipper();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ringskipperCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(ringskipperCard.getId()));
    }

    // ===== Lost clash — stays in the graveyard =====

    @Test
    @DisplayName("Losing the clash leaves Ringskipper in the graveyard")
    void lostClashStaysInGraveyard() {
        // Lower mana value on top for player1 (Forest MV 0 < Grizzly Bears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        Card ringskipperCard = killRingskipper();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ringskipperCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(ringskipperCard.getId()));
    }

    // ===== Tie — a clash is only won on a strictly greater mana value (CR 701.29c) =====

    @Test
    @DisplayName("An equal mana value tie is not a win, so Ringskipper stays in the graveyard")
    void tiedClashStaysInGraveyard() {
        // Equal mana values (both Grizzly Bears MV 2) → no one wins the clash.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        Card ringskipperCard = killRingskipper();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ringskipperCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(ringskipperCard.getId()));
    }
}
