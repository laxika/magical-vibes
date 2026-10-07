package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HardyVeteran;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpireWinder.class, Forest.class, HardyVeteran.class})
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

        harness.castFromHand(player1, new HardyVeteran(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, winder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, winder)).isEqualTo(4);
    }

    @Test
    @DisplayName("Spire Winder itself can be the tenth permanent and the blessing persists")
    void ascendsOnEnteringAndRetainsBonusBelowTenPermanents() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.castFromHand(player1, new SpireWinder(), "{3}{U}");
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.passBothPriorities();

        Permanent winder = gd.playerBattlefields.get(player1.getId()).get(9);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, winder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, winder)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(0);
        harness.runStateBasedActions();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, winder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, winder)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's permanents and blessing do not enable Spire Winder's bonus")
    void onlyControllersPermanentsAndBlessingCount() {
        Permanent winder = harness.addToBattlefieldAndReturn(player1, new SpireWinder());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        Permanent opposingWinder = harness.enterBattlefieldAndReturn(player2, new SpireWinder());

        assertThat(gd.playersWithCityBlessing).contains(player2.getId()).doesNotContain(player1.getId());
        assertThat(gqs.getEffectivePower(gd, opposingWinder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingWinder)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, winder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, winder)).isEqualTo(3);
    }
}
