package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnappingGnarlid.class, Forest.class})
class SnappingGnarlidTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Snapping Gnarlid +1/+1 until end of turn")
    void landfallBoostsSelf() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gnarlid.getEffectivePower()).isEqualTo(3);
        assertThat(gnarlid.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Snapping Gnarlid")
    void opponentLandDoesNotTrigger() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gnarlid.getEffectivePower()).isEqualTo(2);
        assertThat(gnarlid.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gnarlid.getEffectivePower()).isEqualTo(2);
        assertThat(gnarlid.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A land entering without being played triggers landfall and uses the stack")
    void landEnteringWithoutBeingPlayedTriggers() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(1);
        assertThat(gnarlid.getEffectivePower()).isEqualTo(2);
        assertThat(gnarlid.getEffectiveToughness()).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gnarlid.getEffectivePower()).isEqualTo(3);
        assertThat(gnarlid.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple land entries give cumulative boosts only to each trigger's source")
    void multipleLandEntriesBoostEachGnarlid() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A nonland entering does not trigger landfall")
    void nonlandEnteringDoesNotTrigger() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());

        harness.enterBattlefieldAndReturn(player1, new SnappingGnarlid());

        assertThat(gd.stack).isEmpty();
        assertThat(gnarlid.getEffectivePower()).isEqualTo(2);
        assertThat(gnarlid.getEffectiveToughness()).isEqualTo(2);
    }
}
