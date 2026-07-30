package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StonehornDignitaryTest extends BaseCardTest {

    @Test
    @DisplayName("Entering flags the opponent to skip their next combat phase")
    void entersFlagsOpponent() {
        harness.setHand(player1, List.of(new StonehornDignitary()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("The controller is not a legal target for the enter trigger")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new StonehornDignitary()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("The flagged opponent jumps from precombat main straight to postcombat main")
    void flaggedOpponentSkipsCombat() {
        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bear);

        gd.skipNextCombatPhaseCount.put(player2.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }
}
