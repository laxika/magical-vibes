package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZuberiGoldenFeather.class, EkunduGriffin.class, ZhalfirinKnight.class})
class ZuberiGoldenFeatherTest extends BaseCardTest {

    @Test
    @DisplayName("Other Griffins you control get +1/+1")
    void boostsOwnGriffins() {
        addCreatureReady(player1, new ZuberiGoldenFeather());
        Permanent griffin = addCreatureReady(player1, new EkunduGriffin());

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponents' Griffins get +1/+1 too")
    void boostsOpponentGriffins() {
        addCreatureReady(player1, new ZuberiGoldenFeather());
        Permanent griffin = addCreatureReady(player2, new EkunduGriffin());

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Zuberi does not boost itself")
    void doesNotBoostItself() {
        Permanent zuberi = addCreatureReady(player1, new ZuberiGoldenFeather());

        assertThat(gqs.getEffectivePower(gd, zuberi)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zuberi)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Griffin creatures are unaffected")
    void doesNotBoostNonGriffins() {
        addCreatureReady(player1, new ZuberiGoldenFeather());
        Permanent knight = addCreatureReady(player1, new ZhalfirinKnight());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }
}
