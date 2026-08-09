package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

class SliptideSerpentTest extends BaseCardTest {

    @Test
    void activateAbilityReturnsSliptideSerpentToItsOwnersHand() {
        harness.addToBattlefield(player1, new SliptideSerpent());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sliptide Serpent");
        harness.assertNotOnBattlefield(player1, "Sliptide Serpent");
    }
}
