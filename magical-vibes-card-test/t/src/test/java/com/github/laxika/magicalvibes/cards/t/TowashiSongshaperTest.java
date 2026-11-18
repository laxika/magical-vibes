package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.i.IronApprentice;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TowashiSongshaper.class, IronApprentice.class, Mountain.class})
class TowashiSongshaperTest extends BaseCardTest {

    @Test
    void anotherArtifactEnteringUnderYourControlBoostsTowashiSongshaper() {
        Permanent songshaper = harness.addToBattlefieldAndReturn(player1, new TowashiSongshaper());

        harness.enterBattlefieldAndReturn(player1, new IronApprentice());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, songshaper)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, songshaper)).isZero();
    }

    @Test
    void opponentArtifactDoesNotTriggerTowashiSongshaper() {
        Permanent songshaper = harness.addToBattlefieldAndReturn(player1, new TowashiSongshaper());

        harness.enterBattlefieldAndReturn(player2, new IronApprentice());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, songshaper)).isZero();
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent songshaper = harness.addToBattlefieldAndReturn(player1, new TowashiSongshaper());

        harness.enterBattlefieldAndReturn(player1, new IronApprentice());
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, songshaper)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, songshaper)).isZero();
    }

    @Test
    void itsOwnEntryDoesNotTriggerTheAbility() {
        Permanent songshaper = harness.enterBattlefieldAndReturn(player1, new TowashiSongshaper());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, songshaper)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, songshaper)).isZero();
    }

    @Test
    void anotherSongshaperBoostsOnlyTheOneAlreadyOnTheBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TowashiSongshaper());

        Permanent second = harness.enterBattlefieldAndReturn(player1, new TowashiSongshaper());
        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, first)).isZero();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
    }

    @Test
    void separateArtifactEntriesProduceCumulativeBoosts() {
        Permanent songshaper = harness.addToBattlefieldAndReturn(player1, new TowashiSongshaper());

        harness.enterBattlefieldAndReturn(player1, new IronApprentice());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new IronApprentice());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, songshaper)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, songshaper)).isZero();
    }

    @Test
    void aNonartifactEnteringUnderYourControlDoesNotTriggerTheAbility() {
        Permanent songshaper = harness.addToBattlefieldAndReturn(player1, new TowashiSongshaper());

        harness.enterBattlefieldAndReturn(player1, new Mountain());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, songshaper)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, songshaper)).isZero();
    }
}
