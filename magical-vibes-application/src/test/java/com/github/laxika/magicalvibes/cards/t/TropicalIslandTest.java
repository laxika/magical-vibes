package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TropicalIsland.class)
class TropicalIslandTest extends BaseCardTest {

    @Test
    @DisplayName("Tropical Island produces green mana")
    void producesGreenMana() {
        Permanent tropicalIsland = addTropicalIslandReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(tropicalIsland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tropical Island produces blue mana")
    void producesBlueMana() {
        Permanent tropicalIsland = addTropicalIslandReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(tropicalIsland.isTapped()).isTrue();
    }

    private Permanent addTropicalIslandReady() {
        Permanent tropicalIsland = new Permanent(new TropicalIsland());
        tropicalIsland.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(tropicalIsland);
        return tropicalIsland;
    }
}
