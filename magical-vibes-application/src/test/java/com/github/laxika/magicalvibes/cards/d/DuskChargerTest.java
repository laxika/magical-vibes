package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskCharger.class, Forest.class, GrizzlyBears.class})
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

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Dusk Charger counts itself as the tenth permanent and grants the blessing immediately")
    void ascendsWhenChargerItselfEntersAsTenthPermanent() {
        Permanent unblessedCharger = harness.addToBattlefieldAndReturn(player2, new DuskCharger());
        int basePower = gqs.getEffectivePower(gd, unblessedCharger);
        int baseToughness = gqs.getEffectiveToughness(gd, unblessedCharger);
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        Permanent charger = harness.enterBattlefieldAndReturn(player1, new DuskCharger());

        assertThat(gd.playersWithCityBlessing).contains(player1.getId()).doesNotContain(player2.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(baseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, unblessedCharger)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, unblessedCharger)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The blessing and bonus persist after dropping below ten permanents")
    void retainsBlessingAndBonusAfterLosingPermanents() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new DuskCharger());
        int basePower = gqs.getEffectivePower(gd, charger);
        int baseToughness = gqs.getEffectiveToughness(gd, charger);
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerGraveyards.get(player1.getId()).add(forest.getCard());
        harness.runStateBasedActions();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("An opponent's ten permanents do not grant the Charger's controller the blessing")
    void doesNotCountOpponentsPermanents() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new DuskCharger());
        int basePower = gqs.getEffectivePower(gd, charger);
        int baseToughness = gqs.getEffectiveToughness(gd, charger);
        for (int i = 0; i < 10; i++) {
            harness.enterBattlefieldAndReturn(player2, new Forest());
        }
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.playersWithCityBlessing).isEmpty();
        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(baseToughness);
    }
}
