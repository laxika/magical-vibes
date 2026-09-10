package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GerrardsBattleCry.class, SoltariFootSoldier.class})
class GerrardsBattleCryTest extends BaseCardTest {

    @Test
    @DisplayName("Activation gives creatures you control +1/+1 and stacks across activations")
    void pumpsOwnCreatures() {
        addBattleCry();
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());
        harness.addMana(player1, ManaColor.WHITE, 6);

        activateAndResolve();

        assertThat(soldier.getPowerModifier()).isEqualTo(1);
        assertThat(soldier.getToughnessModifier()).isEqualTo(1);

        activateAndResolve();

        assertThat(soldier.getPowerModifier()).isEqualTo(2);
        assertThat(soldier.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected")
    void opponentCreaturesUnaffected() {
        addBattleCry();
        Permanent enemySoldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        harness.addMana(player1, ManaColor.WHITE, 3);

        activateAndResolve();

        assertThat(enemySoldier.getPowerModifier()).isEqualTo(0);
        assertThat(enemySoldier.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addBattleCry();
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());
        harness.addMana(player1, ManaColor.WHITE, 3);

        activateAndResolve();

        assertThat(soldier.getPowerModifier()).isEqualTo(1);
        assertThat(soldier.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only creatures present at resolution receive the boost")
    void onlyCreaturesPresentAtResolutionAreBoosted() {
        Permanent battleCry = addBattleCry();
        Permanent existingSoldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());
        harness.addMana(player1, ManaColor.WHITE, 3);

        activateAndResolve();

        Permanent laterSoldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());

        assertThat(existingSoldier.getPowerModifier()).isEqualTo(1);
        assertThat(existingSoldier.getToughnessModifier()).isEqualTo(1);
        assertThat(laterSoldier.getPowerModifier()).isZero();
        assertThat(laterSoldier.getToughnessModifier()).isZero();
        assertThat(battleCry.getPowerModifier()).isZero();
        assertThat(battleCry.getToughnessModifier()).isZero();
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addBattleCry() {
        return harness.addToBattlefieldAndReturn(player1, new GerrardsBattleCry());
    }
}
