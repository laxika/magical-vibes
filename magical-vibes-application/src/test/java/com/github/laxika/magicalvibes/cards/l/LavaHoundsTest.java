package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LavaHoundsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to you")
    void etbDeals4DamageToController() {
        harness.setHand(player1, List.of(new LavaHounds()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        gs.playCard(gd, player1, 0, 0, null, null);

        harness.passBothPriorities(); // Resolve creature — ETB triggers
        harness.passBothPriorities(); // Resolve ETB

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lava Hounds"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }
}
