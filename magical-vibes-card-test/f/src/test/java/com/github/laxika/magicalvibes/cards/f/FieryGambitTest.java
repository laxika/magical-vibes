package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieryGambit.class, AlphaMyr.class, Forest.class, Shatter.class})
class FieryGambitTest extends BaseCardTest {

    @Test
    @DisplayName("A lost flip cancels every reward, while stopping after a win deals 3 damage")
    void lossCancelsRewardsAndStoppingAfterWinDealsThreeDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        boolean won = coinFlipLogs().stream().anyMatch(log -> log.contains("wins the coin flip"));
        if (!won) {
            harness.assertOnBattlefield(player2, "Alpha Myr");
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
            return;
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Continuing flips preserves the all-or-nothing reward rule and applies the reached tiers")
    void continuingFlipsAppliesReachedTiersOnlyIfStoppedBeforeLosing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        forest.tap();
        tappedCreature.tap();
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.setLibrary(player1, List.of(
                new AlphaMyr(), new AlphaMyr(), new AlphaMyr(), new AlphaMyr(),
                new AlphaMyr(), new AlphaMyr(), new AlphaMyr(), new AlphaMyr(),
                new AlphaMyr()));
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        while (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            long wins = coinFlipLogs().stream().filter(log -> log.contains("wins the coin flip")).count();
            harness.handleMayAbilityChosen(player1, wins < 3);
        }

        long wins = coinFlipLogs().stream().filter(log -> log.contains("wins the coin flip")).count();
        if (wins >= 3) {
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 6);
            harness.assertNotOnBattlefield(player2, "Alpha Myr");
            assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
            assertThat(forest.isTapped()).isFalse();
        } else {
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
            harness.assertOnBattlefield(player2, "Alpha Myr");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(forest.isTapped()).isTrue();
        }
        assertThat(tappedCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Stopping after two wins applies the first two rewards only")
    void stoppingAfterTwoWinsDoesNotApplyTheThirdReward() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.setLibrary(player1, List.of(
                new AlphaMyr(), new AlphaMyr(), new AlphaMyr(), new AlphaMyr(),
                new AlphaMyr(), new AlphaMyr(), new AlphaMyr(), new AlphaMyr(),
                new AlphaMyr()));
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        while (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            long wins = coinFlipLogs().stream().filter(log -> log.contains("wins the coin flip")).count();
            harness.handleMayAbilityChosen(player1, wins < 2);
        }

        long wins = coinFlipLogs().stream().filter(log -> log.contains("wins the coin flip")).count();
        if (wins >= 2) {
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 6);
            harness.assertNotOnBattlefield(player2, "Alpha Myr");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(forest.isTapped()).isTrue();
        } else {
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
            harness.assertOnBattlefield(player2, "Alpha Myr");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(forest.isTapped()).isTrue();
        }
    }

    @Test
    @DisplayName("Only creatures can be targeted")
    void onlyCreaturesCanBeTargeted() {
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An illegal target prevents resolution before any coin is flipped")
    void removedTargetPreventsAllCoinFlips() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, target.getId());

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alpha Myr");
        harness.assertInGraveyard(player1, "Fiery Gambit");
        assertThat(coinFlipLogs()).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A noncreature permanent cannot be targeted")
    void cannotTargetALand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flipping beyond three wins remains optional and does not multiply the rewards")
    void continuingBeyondThreeWinsAppliesEachRewardOnlyOnce() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        opponentForest.tap();
        harness.setLibrary(player1, List.of(
                new AlphaMyr(), new AlphaMyr(), new AlphaMyr(), new AlphaMyr(),
                new AlphaMyr(), new AlphaMyr(), new AlphaMyr(), new AlphaMyr(),
                new AlphaMyr()));
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        while (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.assertOnBattlefield(player1, "Alpha Myr");
            harness.assertLife(player2, 20);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(forest.isTapped()).isTrue();
            long wins = coinFlipLogs().stream().filter(log -> log.contains("wins the coin flip")).count();
            harness.handleMayAbilityChosen(player1, wins < 4);
        }

        long wins = coinFlipLogs().stream().filter(log -> log.contains("wins the coin flip")).count();
        if (wins == 4) {
            harness.assertNotOnBattlefield(player1, "Alpha Myr");
            harness.assertLife(player2, 14);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
            assertThat(forest.isTapped()).isFalse();
        } else {
            harness.assertOnBattlefield(player1, "Alpha Myr");
            harness.assertLife(player2, 20);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(forest.isTapped()).isTrue();
        }
        harness.assertLife(player1, 20);
        assertThat(opponentForest.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Fiery Gambit");
    }

    private List<String> coinFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Fiery Gambit"))
                .toList();
    }
}
