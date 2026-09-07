package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VolcanicIsland.class)
class VolcanicIslandTest extends BaseCardTest {

    @Test
    @DisplayName("Volcanic Island produces blue mana")
    void producesBlueMana() {
        Permanent volcanicIsland = harness.addToBattlefieldAndReturn(player1, new VolcanicIsland());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(volcanicIsland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Volcanic Island produces red mana")
    void producesRedMana() {
        Permanent volcanicIsland = harness.addToBattlefieldAndReturn(player1, new VolcanicIsland());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(volcanicIsland.isTapped()).isTrue();
    }
}
