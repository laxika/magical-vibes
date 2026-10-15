package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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
        harness.setHand(player2, List.of());

        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new WinterSky(), "{R}");
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Winter Sky");

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
        harness.castFromHand(player1, new WinterSky(), "{R}");
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Winter Sky")).isTrue();
    }

    @Test
    @DisplayName("Winter Sky resolves without creatures and draws the top card only on a loss")
    void resolvesWithoutCreatures() {
        WinterSky firstCard = new WinterSky();
        WinterSky secondCard = new WinterSky();
        harness.setLibrary(player1, List.of(firstCard, new WinterSky()));
        harness.setLibrary(player2, List.of(secondCard, new WinterSky()));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new WinterSky(), "{R}");
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Winter Sky");
        harness.assertLife(player1, won ? 19 : 20);
        harness.assertLife(player2, won ? 19 : 20);
        if (won) {
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerHands.get(player2.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        } else {
            assertThat(gameLogContains("loses the coin flip for Winter Sky")).isTrue();
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
            assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondCard);
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        }
        harness.assertInGraveyard(player1, "Winter Sky");
    }
}
