package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FullThrottleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two combat phases after the main phase and untaps attacked creatures at each one")
    void createsTwoCombatPhasesAndUntapsAttackedCreaturesEachCombat() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));
        assertThat(bear.isTapped()).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FullThrottle()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.additionalCombatPhasesAfterMain).isEqualTo(2);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(bear.isTapped()).isFalse();

        bear.tap();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.additionalCombatPhasesAfterMain).isZero();
        assertThat(bear.isTapped()).isFalse();
    }
}
