package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DuskChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 with the city's blessing")
    void getsPowerAndToughnessBonusWithCityBlessing() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new DuskCharger());
        int basePower = gqs.getEffectivePower(gd, charger);
        int baseToughness = gqs.getEffectiveToughness(gd, charger);
        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Ascend grants the city's blessing when the tenth permanent enters")
    void ascendsWhenTenthPermanentEnters() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new DuskCharger());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        int basePower = gqs.getEffectivePower(gd, charger);
        int baseToughness = gqs.getEffectiveToughness(gd, charger);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(baseToughness);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(baseToughness + 2);
    }
}
