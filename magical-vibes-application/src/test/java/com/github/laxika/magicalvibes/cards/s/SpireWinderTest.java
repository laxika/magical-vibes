package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpireWinderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 with the city's blessing")
    void getsPowerAndToughnessBonusWithCityBlessing() {
        Permanent winder = harness.addToBattlefieldAndReturn(player1, new SpireWinder());

        assertThat(gqs.getEffectivePower(gd, winder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, winder)).isEqualTo(3);

        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, winder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, winder)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ascend grants the city's blessing when the tenth permanent enters")
    void ascendsWhenTenthPermanentEnters() {
        Permanent winder = harness.addToBattlefieldAndReturn(player1, new SpireWinder());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gqs.getEffectivePower(gd, winder)).isEqualTo(2);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, winder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, winder)).isEqualTo(4);
    }
}
