package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RabidBloodsucker.class})
class RabidBloodsuckerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger makes each player lose 2 life")
    void etbMakesEachPlayerLose2Life() {
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new RabidBloodsucker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life loss waits for the entry trigger to resolve")
    void lifeLossWaitsForEntryTriggerToResolve() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 11);
        harness.setHand(player1, List.of(new RabidBloodsucker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 11);

        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 11);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 9);
        assertThat(gd.stack).isEmpty();
    }
}
