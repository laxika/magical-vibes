package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Bayou.class)
class BayouTest extends BaseCardTest {

    @Test
    @DisplayName("Bayou produces black mana")
    void producesBlackMana() {
        Permanent bayou = addBayouReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(bayou.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Bayou produces green mana")
    void producesGreenMana() {
        Permanent bayou = addBayouReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bayou.isTapped()).isTrue();
    }

    private Permanent addBayouReady() {
        Permanent bayou = new Permanent(new Bayou());
        bayou.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bayou);
        return bayou;
    }
}
