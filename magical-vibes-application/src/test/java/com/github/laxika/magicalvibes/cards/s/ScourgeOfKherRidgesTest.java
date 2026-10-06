package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.l.LucentLiminid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScourgeOfKherRidges.class, FomoriNomad.class, LucentLiminid.class})
class ScourgeOfKherRidgesTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability damages creatures without flying")
    void firstAbilityDamagesOnlyNonFlyers() {
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        Permanent groundCreature = addCreatureReady(player2, new FomoriNomad());
        Permanent flyingCreature = addCreatureReady(player2, new LucentLiminid());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scourge of Kher Ridges");
        harness.assertOnBattlefield(player2, "Fomori Nomad");
        harness.assertOnBattlefield(player2, "Lucent Liminid");
        assertThat(scourge.getMarkedDamage()).isZero();
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The second ability damages other creatures with flying")
    void secondAbilityDamagesOnlyOtherFlyers() {
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        Permanent ownFlyingCreature = addCreatureReady(player1, new LucentLiminid());
        Permanent opposingFlyingCreature = addCreatureReady(player2, new LucentLiminid());
        Permanent groundCreature = addCreatureReady(player2, new FomoriNomad());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scourge of Kher Ridges");
        harness.assertNotOnBattlefield(player1, "Lucent Liminid");
        harness.assertNotOnBattlefield(player2, "Lucent Liminid");
        harness.assertOnBattlefield(player2, "Fomori Nomad");
        assertThat(scourge.getMarkedDamage()).isZero();
        assertThat(ownFlyingCreature.getMarkedDamage()).isEqualTo(6);
        assertThat(opposingFlyingCreature.getMarkedDamage()).isEqualTo(6);
        assertThat(groundCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The first ability can be activated repeatedly while tapped and summoning sick")
    void firstAbilityCanBeRepeatedWhileTappedAndSummoningSick() {
        Permanent scourge = harness.addToBattlefieldAndReturn(player1, new ScourgeOfKherRidges());
        scourge.setSummoningSick(true);
        scourge.tap();
        Permanent ownGroundCreature = addCreatureReady(player1, new FomoriNomad());
        Permanent opposingGroundCreature = addCreatureReady(player2, new FomoriNomad());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownGroundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingGroundCreature.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Fomori Nomad");
        harness.assertOnBattlefield(player2, "Fomori Nomad");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fomori Nomad");
        harness.assertInGraveyard(player2, "Fomori Nomad");
        harness.assertNotOnBattlefield(player1, "Fomori Nomad");
        harness.assertNotOnBattlefield(player2, "Fomori Nomad");
        harness.assertOnBattlefield(player1, "Scourge of Kher Ridges");
        assertThat(scourge.getMarkedDamage()).isZero();
        assertThat(scourge.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The second ability excludes only its source, not another Scourge")
    void secondAbilityDamagesAnotherScourge() {
        Permanent scourge = harness.addToBattlefieldAndReturn(player1, new ScourgeOfKherRidges());
        scourge.setSummoningSick(true);
        scourge.tap();
        addCreatureReady(player2, new ScourgeOfKherRidges());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scourge of Kher Ridges");
        harness.assertNotOnBattlefield(player2, "Scourge of Kher Ridges");
        harness.assertInGraveyard(player2, "Scourge of Kher Ridges");
        assertThat(scourge.getMarkedDamage()).isZero();
        assertThat(scourge.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
