package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinterSky.class, LlanowarElves.class})
class WinterSkyTest extends BaseCardTest {

    @Test
    @DisplayName("Exactly one branch resolves: 1 damage to each creature and player, or each player draws")
    void oneBranchResolves() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new WinterSky()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip"));

        if (won) {
            // 1 damage kills both 1/1s and hits both players.
            harness.assertNotOnBattlefield(player1, "Llanowar Elves");
            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore - 1);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore - 1);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        } else {
            harness.assertOnBattlefield(player1, "Llanowar Elves");
            harness.assertOnBattlefield(player2, "Llanowar Elves");
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        }
    }

    @Test
    @DisplayName("Coin flip is logged for Winter Sky")
    void coinFlipLogged() {
        harness.setHand(player1, List.of(new WinterSky()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Winter Sky"));
    }
}
