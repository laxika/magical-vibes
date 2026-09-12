package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinFestival.class, MetathranSoldier.class})
class GoblinFestivalTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage before the flip and transfers on a loss")
    void flipsForDamageOrControl() {
        harness.addToBattlefield(player1, new GoblinFestival());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Goblin Festival");
        boolean lost = gameLogContains("loses the coin flip for Goblin Festival");
        assertThat(won).isNotEqualTo(lost);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        if (won) {
            harness.assertOnBattlefield(player1, "Goblin Festival");
        } else {
            harness.assertOnBattlefield(player2, "Goblin Festival");
            harness.assertNotOnBattlefield(player1, "Goblin Festival");
        }
    }

    @Test
    @DisplayName("Can deal damage to a permanent as an any-target choice")
    void dealsDamageToPermanent() {
        harness.addToBattlefield(player1, new GoblinFestival());
        harness.addToBattlefield(player2, new MetathranSoldier());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Metathran Soldier"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Metathran Soldier");
    }
}
