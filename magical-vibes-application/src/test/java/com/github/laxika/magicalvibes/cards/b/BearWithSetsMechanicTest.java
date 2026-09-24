package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BearWithSetsMechanic.class, GrizzlyBears.class})
class BearWithSetsMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("Aggressive creates a restricted combat after the first combat")
    void createsRestrictedAggressiveCombat() {
        addCreatureReady(player1, new BearWithSetsMechanic());
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId()))
                .contains(0)
                .doesNotContain(1);
    }

    @Test
    @DisplayName("Aggressive does not create a combat after it is absent at first combat end")
    void doesNotCreateCombatWithoutAggressiveCreature() {
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }
}
