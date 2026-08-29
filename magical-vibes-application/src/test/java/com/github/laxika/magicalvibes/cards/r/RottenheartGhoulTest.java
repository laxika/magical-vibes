package com.github.laxika.magicalvibes.cards.r;

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

class RottenheartGhoulTest extends BaseCardTest {

    private void startMainPhaseWithMurder() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, new ArrayList<>(List.of(new Murder(), new Peek())));
        harness.addMana(player1, ManaColor.BLACK, 3);
    }

    @Test
    @DisplayName("When this creature dies, the chosen player discards a card")
    void deathTriggerDiscardsFromChosenPlayer() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new RottenheartGhoul());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        startMainPhaseWithMurder();

        harness.castInstant(player1, 0, ghoul.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The death trigger can target the other player")
    void deathTriggerCanTargetOpponent() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new RottenheartGhoul());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        startMainPhaseWithMurder();

        harness.castInstant(player1, 0, ghoul.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The death trigger does not happen while this creature remains on the battlefield")
    void noDeathNoDiscard() {
        harness.addToBattlefield(player1, new RottenheartGhoul());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        startMainPhaseWithMurder();

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}
