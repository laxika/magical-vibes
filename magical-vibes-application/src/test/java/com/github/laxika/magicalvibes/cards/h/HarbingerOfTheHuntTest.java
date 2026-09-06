package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarbingerOfTheHunt.class, AirElemental.class, GrizzlyBears.class})
class HarbingerOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability damages creatures without flying, but not flyers")
    void damagesCreaturesWithoutFlying() {
        Permanent harbinger = harness.addToBattlefieldAndReturn(player1, new HarbingerOfTheHunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(airElemental.getMarkedDamage()).isZero();
        assertThat(harbinger.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Green ability damages other creatures with flying, but not this creature")
    void damagesOtherCreaturesWithFlying() {
        Permanent harbinger = harness.addToBattlefieldAndReturn(player1, new HarbingerOfTheHunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(airElemental.getMarkedDamage()).isEqualTo(1);
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(harbinger.getMarkedDamage()).isZero();
    }
}
