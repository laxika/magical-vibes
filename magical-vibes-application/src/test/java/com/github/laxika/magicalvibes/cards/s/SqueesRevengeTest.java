package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SqueesRevenge.class, GrizzlyBears.class})
class SqueesRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards for each flip only when every chosen flip is won")
    void drawsOnlyAfterWinningEveryChosenFlip() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new SqueesRevenge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        long flips = coinFlipLogs().size();
        boolean wonEveryFlip = flips == 3
                && coinFlipLogs().stream().allMatch(log -> log.contains("wins the coin flip"));
        assertThat(flips).isBetween(0L, 3L);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(wonEveryFlip ? 6 : 0);
    }

    @Test
    @DisplayName("Choosing zero performs no flips and draws no cards")
    void choosingZeroDoesNothing() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new SqueesRevenge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(coinFlipLogs()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private List<String> coinFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Squee's Revenge"))
                .toList();
    }
}
