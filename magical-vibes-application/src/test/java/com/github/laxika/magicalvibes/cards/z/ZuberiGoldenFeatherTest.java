package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZuberiGoldenFeatherTest extends BaseCardTest {

    @Test
    @DisplayName("Other Griffins you control get +1/+1")
    void boostsOwnGriffins() {
        addCreatureReady(player1, new ZuberiGoldenFeather());
        addCreatureReady(player1, new EkunduGriffin());

        Permanent griffin = findPermanent(player1, "Ekundu Griffin");
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponents' Griffins get +1/+1 too")
    void boostsOpponentGriffins() {
        addCreatureReady(player1, new ZuberiGoldenFeather());
        addCreatureReady(player2, new EkunduGriffin());

        Permanent griffin = findPermanent(player2, "Ekundu Griffin");
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Zuberi does not boost itself")
    void doesNotBoostItself() {
        addCreatureReady(player1, new ZuberiGoldenFeather());

        Permanent zuberi = findPermanent(player1, "Zuberi, Golden Feather");
        assertThat(gqs.getEffectivePower(gd, zuberi)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zuberi)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Griffin creatures are unaffected")
    void doesNotBoostNonGriffins() {
        addCreatureReady(player1, new ZuberiGoldenFeather());
        addCreatureReady(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
