package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ExperimentalConfectioner.class)
class ExperimentalConfectionerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodTokenOnEnter() {
        harness.enterBattlefieldAndReturn(player1, new ExperimentalConfectioner());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Sacrificing a Food creates a Rat token that can't block")
    void sacrificingFoodCreatesNonblockingRat() {
        harness.enterBattlefieldAndReturn(player1, new ExperimentalConfectioner());
        harness.passBothPriorities();

        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(countPermanents(player1, "Food")).isZero();
        Permanent rat = findPermanent(player1, "Rat");
        assertThat(bls.canBlock(gd, rat)).isFalse();
    }
}
