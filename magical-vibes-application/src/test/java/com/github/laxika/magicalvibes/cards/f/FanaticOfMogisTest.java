package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Fanatic of Mogis")
class FanaticOfMogisTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage to each opponent equal to red devotion")
    void etbDealsDamageEqualToRedDevotion() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FanaticOfMogis());
        harness.setHand(player1, List.of(new FanaticOfMogis()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }
}
