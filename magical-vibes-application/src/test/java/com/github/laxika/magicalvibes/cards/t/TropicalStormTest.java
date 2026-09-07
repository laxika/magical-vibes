package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AzimaetDrake;
import com.github.laxika.magicalvibes.cards.c.CrimsonRoc;
import com.github.laxika.magicalvibes.cards.j.JolraelsCentaur;
import com.github.laxika.magicalvibes.cards.m.MerfolkRaiders;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TropicalStorm.class, AzimaetDrake.class, CrimsonRoc.class, MerfolkRaiders.class,
        JolraelsCentaur.class})
class TropicalStormTest extends BaseCardTest {

    private void castStorm(int x) {
        harness.setHand(player1, List.of(new TropicalStorm()));
        harness.addMana(player1, ManaColor.GREEN, x + 1);
        harness.castAndResolveSorcery(player1, 0, x);
    }

    @Test
    @DisplayName("Deals X damage to non-blue flyers")
    void killsNonBlueFlyer() {
        harness.addToBattlefield(player2, new CrimsonRoc());

        castStorm(2);

        harness.assertInGraveyard(player2, "Crimson Roc");
    }

    @Test
    @DisplayName("Deals only 1 damage to blue creatures without flying")
    void blueGroundCreatureTakesOnlyTheAdditionalDamage() {
        Permanent blueGround = harness.addToBattlefieldAndReturn(player2, new MerfolkRaiders());
        Permanent greenGround = harness.addToBattlefieldAndReturn(player2, new JolraelsCentaur());

        castStorm(3);

        assertThat(blueGround.getMarkedDamage()).isEqualTo(1);
        assertThat(greenGround.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Merfolk Raiders");
        harness.assertOnBattlefield(player2, "Jolrael's Centaur");
    }

    @Test
    @DisplayName("Blue flyers take X plus 1 damage")
    void blueFlyerTakesXPlusOne() {
        Permanent blueFlyer = harness.addToBattlefieldAndReturn(player2, new AzimaetDrake());

        castStorm(1);

        assertThat(blueFlyer.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Azimaet Drake");
    }

    @Test
    @DisplayName("Deals no damage to players")
    void dealsNoDamageToPlayers() {
        castStorm(5);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("With X=0 only blue creatures take damage on either battlefield")
    void xZeroOnlyDamagesBlueCreatures() {
        Permanent ownBlueGround = harness.addToBattlefieldAndReturn(player1, new MerfolkRaiders());
        Permanent opposingRedFlyer = harness.addToBattlefieldAndReturn(player2, new CrimsonRoc());
        Permanent opposingGreenGround = harness.addToBattlefieldAndReturn(player2, new JolraelsCentaur());

        castStorm(0);

        assertThat(ownBlueGround.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingRedFlyer.getMarkedDamage()).isZero();
        assertThat(opposingGreenGround.getMarkedDamage()).isZero();
    }
}
