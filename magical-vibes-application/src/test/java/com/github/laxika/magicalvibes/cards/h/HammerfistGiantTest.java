package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HammerfistGiant.class, Watchwolf.class, SnappingDrake.class})
class HammerfistGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it deals 4 damage to each player and each creature without flying")
    void tappingItDamagesPlayersAndGroundCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent giant = addCreatureReady(player1, new HammerfistGiant());
        harness.addToBattlefield(player2, new Watchwolf());
        Permanent snappingDrake = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hammerfist Giant");
        harness.assertInGraveyard(player1, "Hammerfist Giant");
        harness.assertNotOnBattlefield(player2, "Watchwolf");
        assertThat(snappingDrake.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Snapping Drake");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Its ability taps the Giant")
    void activationTapsTheGiant() {
        Permanent giant = addCreatureReady(player1, new HammerfistGiant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(giant.isTapped()).isTrue();
    }
}
