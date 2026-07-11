package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManaClashTest extends BaseCardTest {

    @Test
    @DisplayName("Coin-flip loop terminates and each player loses life equal to their tails")
    void dealsDamagePerTails() {
        int startLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new ManaClash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // The loop resolved fully — nothing left on the stack (no infinite loop).
        assertThat(gd.stack).isEmpty();

        // Each "Mana Clash:" log line is one round: "<controller> flips X, <opponent> flips Y."
        // Damage is tied precisely to tails: life lost == number of that player's tails.
        List<String> rounds = gd.gameLog.stream()
                .filter(line -> line.startsWith("Mana Clash:"))
                .toList();
        assertThat(rounds).isNotEmpty();

        long controllerTails = rounds.stream()
                .filter(line -> line.split(", ")[0].contains("flips tails"))
                .count();
        long opponentTails = rounds.stream()
                .filter(line -> line.split(", ")[1].contains("flips tails"))
                .count();

        // The last round is always both-heads (loop terminator), so it deals no damage.
        assertThat(rounds.get(rounds.size() - 1)).contains("flips heads, ").endsWith("flips heads.");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startLife - (int) controllerTails);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startLife - (int) opponentTails);
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new ManaClash()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
