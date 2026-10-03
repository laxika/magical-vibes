package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TinStreetDodger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaggerCaster.class, TinStreetDodger.class})
class DaggerCasterTest extends BaseCardTest {

    @Test
    void entersAndDealsDamageToEachOpponent() {
        harness.setLife(player2, 20);

        castDaggerCaster();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void entersAndDealsDamageToEachCreatureOpponentsControl() {
        harness.addToBattlefield(player2, new DaggerCaster());

        castDaggerCaster();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Dagger Caster").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void killsOpponentCreaturesWithOneToughness() {
        harness.addToBattlefield(player2, new TinStreetDodger());

        castDaggerCaster();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tin Street Dodger");
        harness.assertInGraveyard(player2, "Tin Street Dodger");
    }

    @Test
    void doesNotDamageCreaturesItsControllerControls() {
        DaggerCaster ownCreature = new DaggerCaster();
        harness.addToBattlefield(player1, ownCreature);

        castDaggerCaster();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dagger Caster").getMarkedDamage()).isZero();
    }

    @Test
    void triggerStillDealsDamageAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new DaggerCaster());

        castDaggerCaster();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(findPermanent(player2, "Dagger Caster").getMarkedDamage()).isZero();
        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Dagger Caster"));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(findPermanent(player2, "Dagger Caster").getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Dagger Caster");
    }

    @Test
    void damagesAllOpponentCreaturesWhileSparingItsControllerAndOwnCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TinStreetDodger());
        harness.addToBattlefield(player2, new TinStreetDodger());
        harness.addToBattlefield(player2, new DaggerCaster());

        castDaggerCaster();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(findPermanent(player1, "Tin Street Dodger").getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Dagger Caster").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Dagger Caster").getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Tin Street Dodger");
    }

    private void castDaggerCaster() {
        harness.castFromHand(player1, new DaggerCaster(), "{3}{R}");
    }
}
