package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent crocodile = new Permanent(new HagraCrocodile());
        crocodile.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(crocodile);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
