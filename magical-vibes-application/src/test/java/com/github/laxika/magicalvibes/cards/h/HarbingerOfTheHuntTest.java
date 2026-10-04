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
    @Test
    @DisplayName("Red ability damages friendly ground creatures without damaging either player")
    void damagesFriendlyGroundCreatures() {
        harness.addToBattlefield(player1, new HarbingerOfTheHunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Repeated green activations kill other Harbingers but leave the source unharmed")
    void repeatedGreenActivationsDamageAllOtherHarbingers() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HarbingerOfTheHunt());
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new HarbingerOfTheHunt());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new HarbingerOfTheHunt());
        harness.addMana(player1, ManaColor.GREEN, 9);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
        }

        assertThat(source.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source).doesNotContain(friendly);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposing);
        harness.assertInGraveyard(player1, "Harbinger of the Hunt");
        harness.assertInGraveyard(player2, "Harbinger of the Hunt");
    }

    @Test
    @DisplayName("Green ability still damages other flyers after its source leaves the battlefield")
    void greenAbilityResolvesWithoutSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HarbingerOfTheHunt());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new HarbingerOfTheHunt());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isEqualTo(1);
    }
}
