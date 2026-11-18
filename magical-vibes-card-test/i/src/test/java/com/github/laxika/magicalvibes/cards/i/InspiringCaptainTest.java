package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Inspiring Captain")
@CardUsed({InspiringCaptain.class, GrizzlyBears.class})
class InspiringCaptainTest extends BaseCardTest {

    private void castCaptain() {
        harness.castFromHand(player1, new InspiringCaptain(), "{3}{W}");
    }

    @Test
    @DisplayName("ETB gives creatures you control +1/+1 until end of turn")
    void etbBoostsOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCaptain();
        resolveAllTriggers();

        Permanent captain = findPermanent(player1, "Inspiring Captain");

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, captain)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, captain)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost creatures an opponent controls")
    void doesNotBoostOpponents() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCaptain();
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, opponentBears)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentBears)).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCaptain();
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(0);
    }

    @Test
    @DisplayName("Creatures entering before the ETB trigger resolves receive the boost")
    void includesCreaturesPresentAtResolution() {
        castCaptain();
        harness.passBothPriorities();

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering after the ETB trigger resolves do not receive the boost")
    void excludesCreaturesEnteringAfterResolution() {
        castCaptain();
        resolveAllTriggers();

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, findPermanent(player1, "Inspiring Captain"))).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, findPermanent(player1, "Inspiring Captain"))).isEqualTo(1);
    }
}
