package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BetorKinToAll.class, AvatarOfMight.class})
class BetorKinToAllTest extends BaseCardTest {

    @Test
    @DisplayName("Does nothing when controlled creatures have total toughness below 10")
    void doesNothingBelowTenToughness() {
        harness.setHand(player1, List.of(new BetorKinToAll()));
        addBetorMana();

        harness.castCreature(player1, 0);
        advanceToEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Draws at 10 toughness and untaps creatures at 20 toughness")
    void drawsAndUntapsAtThresholds() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new AvatarOfMight());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        ownCreature.tap();
        opponentCreature.tap();
        harness.setHand(player1, List.of(new BetorKinToAll()));
        addBetorMana();

        harness.castCreature(player1, 0);
        advanceToEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each opponent loses half their life at 40 toughness, rounded up")
    void opponentsLoseHalfLifeAtFortyToughness() {
        Permanent ownCreature = null;
        for (int i = 0; i < 5; i++) {
            ownCreature = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        }
        ownCreature.tap();
        harness.setLife(player2, 9);
        harness.setHand(player1, List.of(new BetorKinToAll()));
        addBetorMana();

        harness.castCreature(player1, 0);
        advanceToEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(ownCreature.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 4);
    }

    private void addBetorMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void advanceToEndStepTrigger() {
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_STEP));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.END_STEP));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
