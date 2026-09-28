package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShardOfTheNightbringer.class})
class ShardOfTheNightbringerTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, target opponent loses half their life rounded up and the controller gains it")
    void castEtbDrainsHalfLifeRoundedUp() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 21);
        castShard(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("The ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ShardOfTheNightbringer()));
        addManaForCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castShard(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ShardOfTheNightbringer()));
        addManaForCast();
        harness.castCreature(player1, 0, targetId);
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 3);
    }
}
