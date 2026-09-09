package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShopkeepersBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Shopkeeper's Bane gains its controller 2 life")
    void attackGainsTwoLife() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ShopkeepersBane());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }
}
