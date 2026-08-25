package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HammerfistGiant.class, GrizzlyBears.class, AirElemental.class})
class HammerfistGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it deals 4 damage to each player and each creature without flying")
    void tappingItDamagesPlayersAndGroundCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HammerfistGiant());
        giant.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hammerfist Giant");
        harness.assertInGraveyard(player1, "Hammerfist Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(airElemental.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Its ability taps the Giant")
    void activationTapsTheGiant() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HammerfistGiant());
        giant.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(giant.isTapped()).isTrue();
    }
}
