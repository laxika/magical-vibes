package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadbridgeShamanTest extends BaseCardTest {

    // "When this creature dies, target opponent discards a card."

    private void startMainPhaseWithMurder() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, new ArrayList<>(List.of(new Murder())));
        harness.addMana(player1, ManaColor.BLACK, 3);
    }

    @Test
    @DisplayName("Dying makes the target opponent discard a card")
    void deathTriggerDiscards() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DeadbridgeShaman());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        startMainPhaseWithMurder();

        harness.castInstant(player1, 0, shaman.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Empty opponent hand: nothing is discarded")
    void emptyHandDiscardsNothing() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DeadbridgeShaman());
        harness.setHand(player2, new ArrayList<>());
        startMainPhaseWithMurder();

        harness.castInstant(player1, 0, shaman.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Shaman staying on the battlefield does not trigger the discard")
    void noDeathNoDiscard() {
        harness.addToBattlefieldAndReturn(player1, new DeadbridgeShaman());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        startMainPhaseWithMurder();

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}
