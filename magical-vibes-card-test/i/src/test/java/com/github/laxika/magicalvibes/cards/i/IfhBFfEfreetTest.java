package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IfhBFfEfreet.class, AirElemental.class, GrizzlyBears.class, SuntailHawk.class})
class IfhBFfEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each player and each creature with flying")
    void damagesPlayersAndFlyers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new IfhBFfEfreet());
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent nonFlyer = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(efreet.getMarkedDamage()).isEqualTo(1);
        assertThat(ownFlyer.getMarkedDamage()).isEqualTo(1);
        assertThat(nonFlyer.getMarkedDamage()).isZero();
        assertThat(opposingFlyer.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Any player may activate it")
    void anyPlayerMayActivateIt() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new IfhBFfEfreet());
        harness.addToBattlefield(player1, new SuntailHawk());
        Permanent nonFlyer = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Suntail Hawk");
        assertThat(nonFlyer.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Repeated activations resolve even after their tapped source dies")
    void queuedActivationsResolveAfterSourceDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new IfhBFfEfreet());
        efreet.tap();
        efreet.setSummoningSick(true);
        harness.addToBattlefield(player2, new AirElemental());
        Permanent nonFlyer = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Ifh-Bíff Efreet");
        harness.assertInGraveyard(player1, "Ifh-Bíff Efreet");
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(nonFlyer.getMarkedDamage()).isZero();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("An opponent must pay the green activation cost with their own mana")
    void opponentCannotSpendControllersMana() {
        harness.addToBattlefield(player1, new IfhBFfEfreet());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }
}
