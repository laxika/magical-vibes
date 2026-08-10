package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieryGambitTest extends BaseCardTest {

    @Test
    @DisplayName("A lost flip cancels every reward, while stopping after a win deals 3 damage")
    void lossCancelsRewardsAndStoppingAfterWinDealsThreeDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        boolean won = coinFlipLogs().stream().anyMatch(log -> log.contains("wins the coin flip"));
        if (!won) {
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
            return;
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Continuing flips preserves the all-or-nothing reward rule and applies the reached tiers")
    void continuingFlipsAppliesReachedTiersOnlyIfStoppedBeforeLosing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        while (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            long wins = coinFlipLogs().stream().filter(log -> log.contains("wins the coin flip")).count();
            harness.handleMayAbilityChosen(player1, wins < 3);
        }

        long wins = coinFlipLogs().stream().filter(log -> log.contains("wins the coin flip")).count();
        if (wins >= 3) {
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 6);
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
            assertThat(forest.isTapped()).isFalse();
        } else {
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
            harness.assertOnBattlefield(player2, "Grizzly Bears");
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

    private List<String> coinFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Fiery Gambit"))
                .toList();
    }
}
