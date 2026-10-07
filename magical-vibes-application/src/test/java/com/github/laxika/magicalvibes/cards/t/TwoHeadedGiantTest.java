package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChanceEncounter;
import com.github.laxika.magicalvibes.cards.k.KrarksThumb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@CardUsed({TwoHeadedGiant.class, ChanceEncounter.class, KrarksThumb.class})
class TwoHeadedGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger puts triggered ability on the stack")
    void attackPutsTriggeredAbilityOnStack() {
        addCreatureReady(player1, new TwoHeadedGiant());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.stream()
                .anyMatch(entry -> entry.getCard().getName().equals("Two-Headed Giant")))
                .isTrue();
    }

    @Test
    @DisplayName("Attack trigger flips two coins and logs the result")
    void attackFlipsTwoCoinsAndLogs() {
        addCreatureReady(player1, new TwoHeadedGiant());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("flips two coins for Two-Headed Giant"));
    }

    @Test
    @DisplayName("Both heads grants double strike, both tails grants menace, mixed grants neither")
    void coinFlipOutcomesAreConsistent() {
        Permanent giant = addCreatureReady(player1, new TwoHeadedGiant());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        boolean hasDoubleStrike = giant.hasKeyword(Keyword.DOUBLE_STRIKE);
        boolean hasMenace = giant.hasKeyword(Keyword.MENACE);

        // Double strike and menace are mutually exclusive outcomes
        assertThat(hasDoubleStrike && hasMenace)
                .as("Cannot have both double strike and menace from the same flip")
                .isFalse();

        if (hasDoubleStrike) {
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("heads and heads"));
        } else if (hasMenace) {
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("tails and tails"));
        } else {
            // Mixed result — one heads, one tails
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                    log.contains("heads and tails") || log.contains("tails and heads"));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Granted keywords are lost at end of turn")
    void grantedKeywordsLostAtEndOfTurn(boolean heads) {
        Permanent giant = addCreatureReady(player1, new TwoHeadedGiant());
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(heads);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            assertThat(gqs.hasKeyword(gd, giant, heads ? Keyword.DOUBLE_STRIKE : Keyword.MENACE)).isTrue();
            harness.passUntil(player2, TurnStep.UPKEEP);
            assertThat(gqs.hasKeyword(gd, giant, Keyword.DOUBLE_STRIKE)).isFalse();
            assertThat(gqs.hasKeyword(gd, giant, Keyword.MENACE)).isFalse();
        }
    }

    @ParameterizedTest
    @CsvSource({"true, true", "false, false", "true, false", "false, true"})
    void everyCoinOutcomeGrantsOnlyTheAppropriateKeyword(boolean first, boolean second) {
        Permanent giant = addCreatureReady(player1, new TwoHeadedGiant());
        Permanent otherGiant = addCreatureReady(player1, new TwoHeadedGiant());
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(first, second);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            assertThat(gqs.hasKeyword(gd, giant, Keyword.DOUBLE_STRIKE)).isEqualTo(first && second);
            assertThat(gqs.hasKeyword(gd, giant, Keyword.MENACE)).isEqualTo(!first && !second);
            assertThat(gqs.hasKeyword(gd, otherGiant, Keyword.DOUBLE_STRIKE)).isFalse();
            assertThat(gqs.hasKeyword(gd, otherGiant, Keyword.MENACE)).isFalse();
        }
    }

    @Test
    void headsResultsDoNotWinCoinFlips() {
        addCreatureReady(player1, new TwoHeadedGiant());
        Permanent encounter = harness.addToBattlefieldAndReturn(player1, new ChanceEncounter());
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(true);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(encounter.getCounterCount(CounterType.LUCK)).isZero();
        }
    }

    @Test
    void krarksThumbLetsControllerChooseWhichResultsToIgnore() {
        Permanent giant = addCreatureReady(player1, new TwoHeadedGiant());
        harness.addToBattlefield(player1, new KrarksThumb());
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(true, false, true, false);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            assertThat(gqs.hasKeyword(gd, giant, Keyword.DOUBLE_STRIKE)).isFalse();
        }
    }
}
