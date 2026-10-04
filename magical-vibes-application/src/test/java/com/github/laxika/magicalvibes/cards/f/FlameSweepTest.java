package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CeruleanDrake;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameSweep.class, AirElemental.class, GreenwoodSentinel.class, CeruleanDrake.class})
class FlameSweepTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to every creature except your flying creatures")
    void damagesAllCreaturesExceptOwnFlyers() {
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent ownGroundCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        castFlameSweep();

        assertThat(ownFlyer.getMarkedDamage()).isZero();
        assertThat(ownGroundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingFlyer.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castFlameSweep();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opposing creatures with protection from red prevent the damage")
    void protectionFromRedPreventsDamage() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new CeruleanDrake());
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        castFlameSweep();

        assertThat(drake.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Cerulean Drake");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("The flying exception follows the spell controller")
    void opponentCastingExemptsTheirFlyers() {
        Permanent firstPlayersFlyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent secondPlayersFlyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player2, List.of(new FlameSweep()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0);

        assertThat(firstPlayersFlyer.getMarkedDamage()).isEqualTo(2);
        assertThat(secondPlayersFlyer.getMarkedDamage()).isZero();
    }

    private void castFlameSweep() {
        harness.setHand(player1, List.of(new FlameSweep()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
