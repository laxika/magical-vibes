package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cytoshape;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.k.KarplusanMinotaur;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltenSentry.class, KarplusanMinotaur.class, Cytoshape.class, Humility.class})
class MoltenSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with the coin flip's base stats and keyword")
    void entersWithCoinFlipResult() {
        for (int i = 0; i < 20; i++) {
            int logStart = gd.gameLog.size();
            harness.castFromHand(player1, new MoltenSentry(), "{3}{R}");
            harness.passBothPriorities();

            Permanent sentry = gd.playerBattlefields.get(player1.getId()).getLast();
            String flipLog = gd.gameLog.subList(logStart, gd.gameLog.size()).stream()
                    .map(GameLogEntry::plainText)
                    .filter(log -> log.contains("coin flip for Molten Sentry"))
                    .findFirst()
                    .orElseThrow();
            if (flipLog.contains("comes up heads")) {
                assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(5);
                assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
                assertThat(gqs.hasKeyword(gd, sentry, Keyword.HASTE)).isTrue();
                assertThat(gqs.hasKeyword(gd, sentry, Keyword.DEFENDER)).isFalse();
            } else {
                assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
                assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(5);
                assertThat(gqs.hasKeyword(gd, sentry, Keyword.HASTE)).isFalse();
                assertThat(gqs.hasKeyword(gd, sentry, Keyword.DEFENDER)).isTrue();
            }
        }
    }

    @Test
    @DisplayName("Heads and tails do not trigger winning or losing a coin flip")
    void entryFlipHasNoWinnerOrLoser() {
        harness.addToBattlefield(player1, new KarplusanMinotaur());

        harness.castFromHand(player1, new MoltenSentry(), "{3}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Molten Sentry");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entering without being cast still establishes the coin flip result")
    void entryWithoutCastingEstablishesStatsAndKeyword() {
        Permanent sentry = harness.enterBattlefieldAndReturn(player1, new MoltenSentry());

        harness.assertOnBattlefield(player1, "Molten Sentry");
        assertThat(gd.stack).isEmpty();
        if (gqs.hasKeyword(gd, sentry, Keyword.HASTE)) {
            assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, sentry, Keyword.DEFENDER)).isFalse();
        } else {
            assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(5);
            assertThat(gqs.hasKeyword(gd, sentry, Keyword.DEFENDER)).isTrue();
        }
    }

    @Test
    @DisplayName("A creature already on the battlefield copies the chosen form without another flip")
    void existingCreatureCopiesChosenForm() {
        Permanent sentry = harness.enterBattlefieldAndReturn(player1, new MoltenSentry());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KarplusanMinotaur());
        int power = gqs.getEffectivePower(gd, sentry);
        int toughness = gqs.getEffectiveToughness(gd, sentry);
        boolean haste = gqs.hasKeyword(gd, sentry, Keyword.HASTE);
        int flipsBefore = (int) gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Molten Sentry")).count();
        harness.setHand(player1, List.of(new Cytoshape()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, sentry.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughness);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isEqualTo(haste);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEFENDER)).isEqualTo(!haste);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Molten Sentry")).count())
                .isEqualTo(flipsBefore);
    }

    @Test
    @DisplayName("The flipped form is a copiable value, so an earlier Humility still makes it a 1/1 with no abilities")
    void earlierHumilityOverridesTheFlippedForm() {
        harness.addToBattlefield(player2, new Humility());

        Permanent sentry = harness.enterBattlefieldAndReturn(player1, new MoltenSentry());

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sentry, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sentry, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("No player wins or loses the heads-or-tails entry flip, and the log says so")
    void entryFlipHasNoWinner() {
        harness.castFromHand(player1, new MoltenSentry(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.gameLog).noneMatch(line -> line.plainText().contains("wins the coin flip")
                || line.plainText().contains("loses the coin flip"));
        assertThat(gd.gameLog).anyMatch(line -> line.plainText().matches(
                player1.getUsername() + "'s coin flip for Molten Sentry comes up (heads|tails)\\."));
    }
}
