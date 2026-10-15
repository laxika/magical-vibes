package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Boltwave.class})
class BoltwaveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each opponent, not the controller")
    void dealsThreeToEachOpponent() {
        harness.setHand(player1, List.of(new Boltwave()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent is determined relative to the caster")
    void secondPlayerDamagesOnlyFirstPlayer() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Boltwave()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Boltwave");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lethal damage defeats the opponent while leaving a low-life caster unharmed")
    void lethalDamageDoesNotHarmCaster() {
        harness.setHand(player1, List.of(new Boltwave()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 1);
        harness.setLife(player2, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 1);
        harness.assertLife(player2, -1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}
