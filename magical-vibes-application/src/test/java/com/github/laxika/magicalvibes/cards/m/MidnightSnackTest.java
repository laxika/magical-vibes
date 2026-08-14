package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MidnightSnackTest extends BaseCardTest {

    @Test
    @DisplayName("Raid creates a Food token at the beginning of the controller's end step")
    void raidCreatesFoodToken() {
        harness.addToBattlefield(player1, new MidnightSnack());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Raid does not create a Food token when the controller did not attack")
    void raidDoesNotCreateFoodTokenWithoutAttack() {
        harness.addToBattlefield(player1, new MidnightSnack());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Sacrificing Midnight Snack makes an opponent lose life gained this turn")
    void sacrificesAndLosesLifeEqualToLifeGainedThisTurn() {
        harness.addToBattlefield(player1, new MidnightSnack());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);
        gd.lifeGainedThisTurn.put(player1.getId(), 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Midnight Snack");
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Sacrifice ability cannot target its controller")
    void sacrificeAbilityCannotTargetController() {
        harness.addToBattlefield(player1, new MidnightSnack());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
