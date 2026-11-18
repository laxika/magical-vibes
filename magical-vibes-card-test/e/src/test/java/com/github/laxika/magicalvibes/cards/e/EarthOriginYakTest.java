package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthOriginYak.class, GrizzlyBears.class})
class EarthOriginYakTest extends BaseCardTest {

    private void castYak() {
        harness.castFromHand(player1, new EarthOriginYak(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB gives your creatures +1/+1 until end of turn")
    void etbBoostsOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castYak();

        Permanent yak = findPermanent(player1, "Earth-Origin Yak");

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, yak)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, yak)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not boost creatures an opponent controls")
    void doesNotBoostOpponents() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castYak();

        assertThat(PermanentGrants.powerBoostOf(gd, opponentBears)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentBears)).isZero();
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castYak();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isZero();
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves are not boosted")
    void laterCreaturesAreNotBoosted() {
        castYak();

        Permanent laterYak = harness.enterBattlefieldAndReturn(player1, new EarthOriginYak());

        assertThat(PermanentGrants.powerBoostOf(gd, laterYak)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, laterYak)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, findPermanent(player1, "Earth-Origin Yak"))).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger boosts creatures present at resolution and stacks with another Yak")
    void boostsCreaturesPresentAtResolution() {
        Permanent firstYak = harness.enterBattlefieldAndReturn(player1, new EarthOriginYak());
        Permanent secondYak = harness.enterBattlefieldAndReturn(player1, new EarthOriginYak());

        assertThat(PermanentGrants.powerBoostOf(gd, firstYak)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, secondYak)).isZero();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, firstYak)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstYak)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, secondYak)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondYak)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, firstYak)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstYak)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, secondYak)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondYak)).isEqualTo(2);
    }
}
