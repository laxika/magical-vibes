package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PearledUnicorn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JovialEvil.class, PearledUnicorn.class, GrizzlyBears.class})
class JovialEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Deals twice the number of target opponent's white creatures as damage")
    void dealsDamageBasedOnTargetOpponentsWhiteCreatures() {
        harness.addToBattlefield(player1, new PearledUnicorn());
        harness.addToBattlefield(player2, new PearledUnicorn());
        harness.addToBattlefield(player2, new PearledUnicorn());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castJovialEvil(player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new JovialEvil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castJovialEvil(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new JovialEvil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
