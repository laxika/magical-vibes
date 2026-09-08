package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SquallmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each player and each creature with flying")
    void damagesPlayersAndFlyers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent squallmonger = harness.addToBattlefieldAndReturn(player1, new Squallmonger());
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent nonFlyer = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(squallmonger.getMarkedDamage()).isZero();
        assertThat(ownFlyer.getMarkedDamage()).isEqualTo(1);
        assertThat(nonFlyer.getMarkedDamage()).isZero();
        assertThat(opposingFlyer.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Any player may activate it")
    void anyPlayerMayActivateIt() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Squallmonger());
        harness.addToBattlefield(player1, new SuntailHawk());
        Permanent nonFlyer = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Suntail Hawk");
        assertThat(nonFlyer.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
