package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CliffsideLookout;
import com.github.laxika.magicalvibes.cards.s.SnappingGnarlid;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruWarcaller.class, SnappingGnarlid.class, CliffsideLookout.class})
class TajuruWarcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives your creatures +2/+2 until end of turn")
    void ownAllyEntryBoostsYourCreatures() {
        Permanent gnarlid = addCreatureReady(player1, new SnappingGnarlid());

        Permanent warcaller = harness.enterBattlefieldAndReturn(player1, new TajuruWarcaller());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(4);
    }

    @Test
    @DisplayName("Another Ally entry gives your creatures +2/+2 until end of turn")
    void anotherAllyEntryBoostsYourCreatures() {
        Permanent warcaller = addCreatureReady(player1, new TajuruWarcaller());
        Permanent gnarlid = addCreatureReady(player1, new SnappingGnarlid());

        Permanent ally = harness.enterBattlefieldAndReturn(player1, new CliffsideLookout());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-Ally or opponent Ally entry does not trigger it")
    void nonAllyAndOpponentAllyEntriesDoNotTrigger() {
        Permanent warcaller = addCreatureReady(player1, new TajuruWarcaller());
        Permanent gnarlid = addCreatureReady(player1, new SnappingGnarlid());

        harness.enterBattlefieldAndReturn(player1, new SnappingGnarlid());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player2, new CliffsideLookout());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent gnarlid = addCreatureReady(player1, new SnappingGnarlid());
        harness.enterBattlefieldAndReturn(player1, new TajuruWarcaller());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost applies to creatures present at resolution, but not later arrivals or opponents")
    void boostUsesCreaturesPresentAtResolution() {
        Permanent opponent = addCreatureReady(player2, new SnappingGnarlid());
        Permanent warcaller = harness.enterBattlefieldAndReturn(player1, new TajuruWarcaller());
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new SnappingGnarlid());

        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(2);
        harness.passBothPriorities();

        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new SnappingGnarlid());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Ally entries produce cumulative boosts")
    void multipleAllyEntriesStackBoosts() {
        Permanent warcaller = addCreatureReady(player1, new TajuruWarcaller());
        Permanent gnarlid = addCreatureReady(player1, new SnappingGnarlid());

        Permanent firstAlly = harness.enterBattlefieldAndReturn(player1, new CliffsideLookout());
        harness.passBothPriorities();
        Permanent secondAlly = harness.enterBattlefieldAndReturn(player1, new CliffsideLookout());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, firstAlly)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, firstAlly)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, secondAlly)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondAlly)).isEqualTo(3);
    }
}
