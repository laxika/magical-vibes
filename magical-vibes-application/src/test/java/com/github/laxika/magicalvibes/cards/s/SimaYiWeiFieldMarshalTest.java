package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SimaYiWeiFieldMarshal.class, Swamp.class, Plains.class, UrborgTombOfYawgmoth.class})
class SimaYiWeiFieldMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of Swamps you control; toughness stays 4")
    void powerEqualsControlledSwamps() {
        Permanent simaYi = addCreatureReady(player1, new SimaYiWeiFieldMarshal());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, simaYi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, simaYi)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power counts only your Swamps, not opponent Swamps")
    void countsOnlyControllersSwamps() {
        Permanent simaYi = addCreatureReady(player1, new SimaYiWeiFieldMarshal());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, simaYi)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, simaYi)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power counts lands that gain the Swamp subtype")
    void countsLandsWithSwampSubtypeGrantedByUrborg() {
        Permanent simaYi = addCreatureReady(player1, new SimaYiWeiFieldMarshal());
        harness.addToBattlefield(player1, new UrborgTombOfYawgmoth());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, simaYi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, simaYi)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power updates when Swamps change; toughness stays 4")
    void powerUpdatesWhenSwampsChange() {
        Permanent simaYi = addCreatureReady(player1, new SimaYiWeiFieldMarshal());
        assertThat(gqs.getEffectivePower(gd, simaYi)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, simaYi)).isEqualTo(4);

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, simaYi)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gqs.getEffectivePower(gd, simaYi)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, simaYi)).isEqualTo(4);
    }

}
