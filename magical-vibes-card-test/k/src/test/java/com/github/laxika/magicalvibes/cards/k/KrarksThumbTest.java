package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.FieryGambit;
import com.github.laxika.magicalvibes.cards.m.ManaCrypt;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@CardUsed({KrarksThumb.class, FieryGambit.class, AlphaMyr.class, ManaCrypt.class})
class KrarksThumbTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces a logical coin flip with two flips and ignores one")
    void replacesCoinFlip() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player1, new KrarksThumb());
        castFieryGambit(target);

        assertThat(coinFlipLogs()).anyMatch(log -> log.contains(
                "coin flip for Fiery Gambit (flipped 2 coins and ignored 1)"));
    }

    @Test
    @DisplayName("A Thumb controlled by the opponent does not replace your coin flip")
    void onlyTheFlippersControllerBenefitsFromThumb() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player2, new KrarksThumb());
        castFieryGambit(target);

        assertThat(coinFlipLogs()).isNotEmpty()
                .noneMatch(log -> log.contains("(flipped 2 coins and ignored 1)"));
    }

    @Test
    @DisplayName("A tapped Thumb still replaces coin flips")
    void tappedThumbStillApplies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        Permanent thumb = harness.addToBattlefieldAndReturn(player1, new KrarksThumb());
        thumb.tap();
        castFieryGambit(target);

        assertThat(coinFlipLogs()).anyMatch(log -> log.contains("(flipped 2 coins and ignored 1)"));
    }

    @Test
    @DisplayName("A Thumb that has lost all abilities does not replace coin flips")
    void thumbWithoutAbilitiesDoesNotApply() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        Permanent thumb = harness.addToBattlefieldAndReturn(player1, new KrarksThumb());
        thumb.setLosesAllAbilitiesUntilEndOfTurn(true);
        castFieryGambit(target);

        assertThat(coinFlipLogs()).isNotEmpty()
                .noneMatch(log -> log.contains("(flipped 2 coins and ignored 1)"));
    }

    @ParameterizedTest
    @CsvSource({"Heads, 20", "Tails, 17"})
    @DisplayName("When the two flips disagree, the controller chooses which result to keep")
    void controllerChoosesWhichResultToKeep(String kept, int expectedLife) {
        harness.addToBattlefield(player1, new ManaCrypt());
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.setLife(player1, 20);
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextBoolean()).thenReturn(false, true);
        try (var coins = mockStatic(ThreadLocalRandom.class)) {
            coins.when(ThreadLocalRandom::current).thenReturn(random);
            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                    .isEqualTo(player1.getId());
            harness.assertLife(player1, 20);
            harness.handleListChoice(player1, kept);
        }

        harness.assertLife(player1, expectedLife);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Mana Crypt (flipped 2 coins and ignored 1)")))
                .hasSize(1);
    }

    private void castFieryGambit(Permanent target) {
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        while (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
    }

    private List<String> coinFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Fiery Gambit"))
                .toList();
    }
}
