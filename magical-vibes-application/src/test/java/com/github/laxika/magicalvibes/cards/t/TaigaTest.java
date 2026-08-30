package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Taiga.class)
class TaigaTest extends BaseCardTest {

    @Test
    @DisplayName("Taiga produces red mana")
    void producesRedMana() {
        Permanent taiga = addTaigaReady();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(taiga.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taiga produces green mana")
    void producesGreenMana() {
        Permanent taiga = addTaigaReady();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(taiga.isTapped()).isTrue();
    }

    private Permanent addTaigaReady() {
        Permanent taiga = new Permanent(new Taiga());
        taiga.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(taiga);
        return taiga;
    }
}
