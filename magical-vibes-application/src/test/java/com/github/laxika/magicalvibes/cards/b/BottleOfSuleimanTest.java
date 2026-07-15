package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BottleOfSuleimanTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sacrifices the artifact and produces exactly one flip outcome")
    void activatingFlipsCoin() {
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Sacrifice is a cost, so the Bottle is always gone from the battlefield.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bottle of Suleiman"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bottle of Suleiman"));

        boolean hasDjinn = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Djinn"));
        boolean took5 = gd.playerLifeTotals.get(player1.getId()) == lifeBefore - 5;

        // Exactly one branch resolves.
        assertThat(hasDjinn != took5)
                .as("Either create a 5/5 Djinn (win) or take 5 damage (loss)")
                .isTrue();

        if (hasDjinn) {
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("wins the coin flip"));
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        } else {
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("loses the coin flip"));
        }
    }

    @Test
    @DisplayName("Coin flip is logged for Bottle of Suleiman")
    void coinFlipLogged() {
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("coin flip for Bottle of Suleiman"));
    }
}
