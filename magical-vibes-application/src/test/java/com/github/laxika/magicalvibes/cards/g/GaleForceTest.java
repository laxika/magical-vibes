package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaleForce.class, AirElemental.class, SerraAngel.class, GrizzlyBears.class, GiantSpider.class,
        MahamotiDjinn.class, Levitation.class, Boomerang.class})
class GaleForceTest extends BaseCardTest {

    private void castGaleForce() {
        harness.castFromHand(player1, new GaleForce(), "{4}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gale Force kills flying creatures on both sides")
    void killsFlyers() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new SerraAngel());

        castGaleForce();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Gale Force deals exactly 5 damage to a surviving flying creature")
    void dealsExactlyFiveDamageToSurvivingFlyer() {
        var mahamotiDjinn = harness.addToBattlefieldAndReturn(player2, new MahamotiDjinn());

        castGaleForce();

        harness.assertOnBattlefield(player2, "Mahamoti Djinn");
        assertThat(mahamotiDjinn.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Gale Force leaves non-flying creatures alone")
    void sparesNonFlyers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castGaleForce();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Gale Force deals no damage to players")
    void dealsNoDamageToPlayers() {
        castGaleForce();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Gale Force damages creatures with granted flying and spares grounded creatures")
    void damagesCreaturesWithGrantedFlying() {
        harness.addToBattlefield(player2, new Levitation());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var groundedSpider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        castGaleForce();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Levitation");
        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(groundedSpider.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Gale Force checks flying at resolution after Levitation is returned to hand")
    void sparesCreatureThatLostFlyingBeforeResolution() {
        var levitation = harness.addToBattlefieldAndReturn(player2, new Levitation());
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.castFromHand(player1, new GaleForce(), "{4}{G}");

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, levitation.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Levitation");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Gale Force");
    }
}
