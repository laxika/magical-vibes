package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Tundra.class)
class TundraTest extends BaseCardTest {

    @Test
    @DisplayName("Tundra produces white mana")
    void producesWhiteMana() {
        Permanent tundra = addTundraReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(tundra.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tundra produces blue mana")
    void producesBlueMana() {
        Permanent tundra = addTundraReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(tundra.isTapped()).isTrue();
    }

    private Permanent addTundraReady() {
        Permanent tundra = new Permanent(new Tundra());
        tundra.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(tundra);
        return tundra;
    }
}
