package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NukaColaVendingMachine.class)
class NukaColaVendingMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token from its activated ability")
    void createsFoodToken() {
        harness.addToBattlefield(player1, new NukaColaVendingMachine());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Creates a tapped Treasure token when you sacrifice a Food")
    void createsTappedTreasureWhenFoodIsSacrificed() {
        harness.addToBattlefield(player1, new NukaColaVendingMachine());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isTrue();
    }
}
