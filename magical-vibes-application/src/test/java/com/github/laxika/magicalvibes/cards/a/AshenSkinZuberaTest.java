package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class AshenSkinZuberaTest extends BaseCardTest {

    // "When this creature dies, target opponent discards a card for each Zubera that died this turn."

    private void startMainPhase(int murders) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        List<com.github.laxika.magicalvibes.model.Card> hand = new ArrayList<>();
        for (int i = 0; i < murders; i++) {
            hand.add(new Murder());
        }
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLACK, 6);
    }

    @Test
    @DisplayName("Dies alone: target opponent discards one card")
    void diesAloneDiscardsOne() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new AshenSkinZubera());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        startMainPhase(1);

        harness.castInstant(player1, 0, zubera.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts every Zubera that died this turn, including itself")
    void countsAllZuberaDeathsThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AshenSkinZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AshenSkinZubera());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Peek())));
        startMainPhase(2);

        harness.castInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.castInstant(player1, 0, second.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Non-Zubera deaths do not increase the discard count")
    void nonZuberaDeathsDoNotCount() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new AshenSkinZubera());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        startMainPhase(2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, zubera.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
