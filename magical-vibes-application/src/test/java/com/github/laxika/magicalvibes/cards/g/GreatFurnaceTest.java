package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreatFurnaceTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Great Furnace adds red mana")
    void tapForRedMana() {
        Permanent furnace = harness.addToBattlefieldAndReturn(player1, new GreatFurnace());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(furnace.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
