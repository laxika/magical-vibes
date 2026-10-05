package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MutalithVortexBeast.class, Forest.class, GoForTheThroat.class})
class MutalithVortexBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Flips once for the opponent and resolves the matching branch")
    void flipsForEachOpponentAndResolvesBranch() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new MutalithVortexBeast(), "{4}{U}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        long wins = gd.gameLog.stream()
                .filter(log -> log.plainText().contains("wins the coin flip for Mutalith Vortex Beast"))
                .count();
        long losses = gd.gameLog.stream()
                .filter(log -> log.plainText().contains("loses the coin flip for Mutalith Vortex Beast"))
                .count();

        assertThat(wins + losses).isEqualTo(1);
        if (wins == 1) {
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        } else {
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        }
    }

    @Test
    @DisplayName("Warp Vortex waits for resolution and still resolves after its source is destroyed")
    void triggerResolvesAfterSourceIsDestroyed() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new MutalithVortexBeast(), "{4}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.gameLog).noneMatch(log -> log.plainText().contains("the coin flip for Mutalith Vortex Beast"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Mutalith Vortex Beast"));
        harness.assertInGraveyard(player1, "Mutalith Vortex Beast");
        harness.assertNotOnBattlefield(player1, "Mutalith Vortex Beast");
        assertThat(gd.gameLog).noneMatch(log -> log.plainText().contains("the coin flip for Mutalith Vortex Beast"));

        harness.passBothPriorities();

        long wins = gd.gameLog.stream()
                .filter(log -> log.plainText().contains("wins the coin flip for Mutalith Vortex Beast"))
                .count();
        long losses = gd.gameLog.stream()
                .filter(log -> log.plainText().contains("loses the coin flip for Mutalith Vortex Beast"))
                .count();
        assertThat(wins + losses).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize((int) wins);
        harness.assertLife(player2, wins == 1 ? 20 : 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Warp Vortex uses the entering creature's controller for wins and losses")
    void resolvesForSecondPlayer() {
        harness.forceActivePlayer(player2);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castFromHand(player2, new MutalithVortexBeast(), "{4}{U}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        long wins = gd.gameLog.stream()
                .filter(log -> log.plainText().contains("wins the coin flip for Mutalith Vortex Beast"))
                .count();
        long losses = gd.gameLog.stream()
                .filter(log -> log.plainText().contains("loses the coin flip for Mutalith Vortex Beast"))
                .count();
        assertThat(wins + losses).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize((int) wins);
        harness.assertLife(player1, wins == 1 ? 20 : 17);
        harness.assertLife(player2, 20);
    }
}
