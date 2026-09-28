package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NukaColaVendingMachine.class})
class NukaColaVendingMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token from its activated ability")
    void createsFoodToken() {
        Permanent vendingMachine = harness.addToBattlefieldAndReturn(player1, new NukaColaVendingMachine());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(vendingMachine), 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(vendingMachine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates a tapped Treasure whenever you sacrifice a Food")
    void createsTappedTreasureWhenFoodIsSacrificed() {
        Permanent vendingMachine = harness.addToBattlefieldAndReturn(player1, new NukaColaVendingMachine());
        createFoodToken(vendingMachine);

        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(food), 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isTrue();
    }

    private void createFoodToken(Permanent vendingMachine) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(vendingMachine), 0, null, null);
        harness.passBothPriorities();
        vendingMachine.untap();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
