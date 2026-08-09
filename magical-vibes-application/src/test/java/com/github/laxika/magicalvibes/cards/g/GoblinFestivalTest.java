package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinFestivalTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage before the flip and transfers on a loss")
    void flipsForDamageOrControl() {
        harness.addToBattlefield(player1, new GoblinFestival());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Goblin Festival"));
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        if (won) {
            harness.assertOnBattlefield(player1, "Goblin Festival");
        } else {
            harness.assertOnBattlefield(player2, "Goblin Festival");
            harness.assertNotOnBattlefield(player1, "Goblin Festival");
        }
    }
}
