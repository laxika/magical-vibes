package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@CardUsed({SqueesRevenge.class, Index.class})
class SqueesRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards for each flip only when every chosen flip is won")
    void drawsOnlyAfterWinningEveryChosenFlip() {
        harness.setLibrary(player1, List.of(
                new Index(), new Index(), new Index(),
                new Index(), new Index(), new Index()));
        castAndChooseNumber(3);

        long flips = coinFlipLogs().size();
        boolean wonEveryFlip = flips == 3
                && coinFlipLogs().stream().allMatch(log -> log.contains("wins the coin flip"));
        assertThat(flips).isBetween(0L, 3L);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(wonEveryFlip ? 6 : 0);
    }

    @Test
    @DisplayName("Choosing zero performs no flips and draws no cards")
    void choosingZeroDoesNothing() {
        harness.setLibrary(player1, List.of(new Index(), new Index()));
        castAndChooseNumber(0);

        assertThat(coinFlipLogs()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Winning all three flips draws six cards")
    void winningAllFlipsDrawsTwiceTheChosenNumber() {
        harness.setLibrary(player1, List.of(
                new Index(), new Index(), new Index(),
                new Index(), new Index(), new Index(), new Index()));
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(true);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            castAndChooseNumber(3);

            assertThat(coinFlipLogs()).hasSize(3)
                    .allMatch(log -> log.contains("wins the coin flip"));
            assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            harness.assertInGraveyard(player1, "Squee's Revenge");
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }
    }

    @Test
    @DisplayName("Losing the first flip stops immediately without drawing")
    void losingFirstFlipStopsWithoutDrawing() {
        harness.setLibrary(player1, List.of(new Index(), new Index()));
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(false, true);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            castAndChooseNumber(3);

            assertThat(coinFlipLogs()).hasSize(1);
            assertThat(coinFlipLogs().getFirst()).contains("loses the coin flip");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
            harness.assertInGraveyard(player1, "Squee's Revenge");
        }
    }

    @Test
    @DisplayName("A loss after a win stops flipping and forfeits the entire draw")
    void losingAfterAWinDrawsNothing() {
        harness.setLibrary(player1, List.of(new Index(), new Index()));
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(true, false, true);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            castAndChooseNumber(3);

            assertThat(coinFlipLogs()).hasSize(2);
            assertThat(coinFlipLogs().get(0)).contains("wins the coin flip");
            assertThat(coinFlipLogs().get(1)).contains("loses the coin flip");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
            harness.assertInGraveyard(player1, "Squee's Revenge");
        }
    }

    private void castAndChooseNumber(int chosenNumber) {
        harness.castFromHand(player1, new SqueesRevenge(), "{1}{U}{R}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, chosenNumber);
    }

    private List<String> coinFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Squee's Revenge"))
                .toList();
    }
}
