package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderDragon.class, GrizzlyBears.class, GiantSpider.class, AirElemental.class})
class ThunderDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 3 damage to each creature without flying, killing small ones")
    void etbKillsNonFlyers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castThunderDragon();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB damages larger non-flyers without killing them")
    void etbDamagesLargeNonFlyers() {
        harness.addToBattlefield(player2, new GiantSpider());
        castThunderDragon();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB does not damage creatures with flying")
    void etbSparesFlyers() {
        harness.addToBattlefield(player2, new AirElemental());
        castThunderDragon();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent flyer = findPermanent(player2, "Air Elemental");
        assertThat(flyer.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB damages non-flyers on both sides and leaves players and flyers unharmed")
    void etbAffectsBothControllers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());
        castThunderDragon();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Air Elemental").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Thunder Dragon").getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage waits for the ETB trigger to resolve")
    void damageIsASeparateTrigger() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castThunderDragon();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thunder Dragon");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isZero();

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB resolves when there are no creatures without flying")
    void etbResolvesWithoutNonFlyers() {
        castThunderDragon();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thunder Dragon");
        assertThat(findPermanent(player1, "Thunder Dragon").getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castThunderDragon() {
        harness.castFromHand(player1, new ThunderDragon(), "{5}{R}{R}");
    }
}
