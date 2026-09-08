package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MoltenSentry.class)
class MoltenSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with the coin flip's base stats and keyword")
    void entersWithCoinFlipResult() {
        Set<String> outcomes = new HashSet<>();

        for (int i = 0; i < 20 && outcomes.size() < 2; i++) {
            int logStart = gd.gameLog.size();
            harness.castFromHand(player1, new MoltenSentry(), "{3}{R}");
            harness.passBothPriorities();

            Permanent sentry = gd.playerBattlefields.get(player1.getId()).getLast();
            String flipLog = gd.gameLog.subList(logStart, gd.gameLog.size()).stream()
                    .map(GameLogEntry::plainText)
                    .filter(log -> log.contains("coin flip for Molten Sentry"))
                    .findFirst()
                    .orElseThrow();
            if (flipLog.contains("wins the coin flip")) {
                outcomes.add("heads");
                assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(5);
                assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
                assertThat(gqs.hasKeyword(gd, sentry, Keyword.HASTE)).isTrue();
                assertThat(gqs.hasKeyword(gd, sentry, Keyword.DEFENDER)).isFalse();
            } else {
                outcomes.add("tails");
                assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
                assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(5);
                assertThat(gqs.hasKeyword(gd, sentry, Keyword.HASTE)).isFalse();
                assertThat(gqs.hasKeyword(gd, sentry, Keyword.DEFENDER)).isTrue();
            }
        }

        assertThat(outcomes).containsExactlyInAnyOrder("heads", "tails");
    }
}
