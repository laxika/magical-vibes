package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BetorKinToAll.class, AvatarOfMight.class})
class BetorKinToAllTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void clearStartingHand() {
        harness.setHand(player1, java.util.List.of());
    }

    @Test
    @DisplayName("Does nothing when controlled creatures have total toughness below 10")
    void doesNothingBelowTenToughness() {
        harness.castFromHand(player1, new BetorKinToAll(), "{2}{W}{B}{G}");
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
        harness.castFromHand(player1, new BetorKinToAll(), "{2}{W}{B}{G}");
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
        harness.castFromHand(player1, new BetorKinToAll(), "{2}{W}{B}{G}");
        advanceToEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(ownCreature.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 4);
    }

    @ParameterizedTest
    @CsvSource({"10, true, 20", "19, true, 20", "20, false, 20", "39, false, 20", "40, false, 10"})
    void checksExactToughnessThresholds(int toughness, boolean remainsTapped, int opponentLife) {
        Permanent betor = harness.addToBattlefieldAndReturn(player1, new BetorKinToAll());
        betor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, toughness - 7);
        betor.tap();

        advanceToEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(betor.isTapped()).isEqualTo(remainsTapped);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, opponentLife);
    }

    @Test
    void doesNotTriggerBelowTenEvenWithOpposingCreatures() {
        harness.addToBattlefield(player1, new BetorKinToAll());
        harness.addToBattlefield(player2, new AvatarOfMight());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNothingIfToughnessFallsBelowTenBeforeResolution() {
        Permanent betor = harness.addToBattlefieldAndReturn(player1, new BetorKinToAll());
        betor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(player1, TurnStep.END_STEP);
            assertThat(gd.stack).hasSize(1);
            betor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
            harness.passBothPriorities();
        });

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void higherThresholdsUseToughnessAtResolution() {
        Permanent betor = harness.addToBattlefieldAndReturn(player1, new BetorKinToAll());
        betor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        betor.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(player1, TurnStep.END_STEP);
            assertThat(gd.stack).hasSize(1);
            betor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 33);
            harness.passBothPriorities();
        });

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(betor.isTapped()).isFalse();
        harness.assertLife(player2, 10);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent betor = harness.addToBattlefieldAndReturn(player1, new BetorKinToAll());
        betor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 33);
        betor.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(betor.isTapped()).isTrue();
        harness.assertLife(player2, 20);
    }

    private void advanceToEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(player1, TurnStep.END_STEP);
            if (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            }
        });
    }
}
