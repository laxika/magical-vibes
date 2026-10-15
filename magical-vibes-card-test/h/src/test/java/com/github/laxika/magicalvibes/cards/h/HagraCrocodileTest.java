package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HagraCrocodile.class, Forest.class, GrizzlyBears.class})
class HagraCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Hagra Crocodile +2/+2 until end of turn")
    void landfallBoostsHagraCrocodile() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new HagraCrocodile());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(crocodile.getEffectivePower()).isEqualTo(5);
        assertThat(crocodile.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new HagraCrocodile());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crocodile.getEffectivePower()).isEqualTo(3);
        assertThat(crocodile.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Hagra Crocodile cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player2, new HagraCrocodile());
        crocodile.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Multiple land entries stack their boosts only on their controller's Crocodile")
    void multipleLandEntriesBoostOnlyControllersCrocodile() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new HagraCrocodile());
        Permanent opposingCrocodile = harness.addToBattlefieldAndReturn(player2, new HagraCrocodile());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(crocodile.getEffectivePower()).isEqualTo(7);
        assertThat(crocodile.getEffectiveToughness()).isEqualTo(5);
        assertThat(opposingCrocodile.getEffectivePower()).isEqualTo(3);
        assertThat(opposingCrocodile.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall waits for resolution before boosting the Crocodile")
    void landfallBoostWaitsForResolution() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new HagraCrocodile());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(crocodile.getEffectivePower()).isEqualTo(3);
        assertThat(crocodile.getEffectiveToughness()).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(crocodile.getEffectivePower()).isEqualTo(5);
        assertThat(crocodile.getEffectiveToughness()).isEqualTo(3);
    }
}
