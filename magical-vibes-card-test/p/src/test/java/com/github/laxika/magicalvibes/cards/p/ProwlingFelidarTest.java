package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProwlingFelidar.class, Forest.class})
class ProwlingFelidarTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on Prowling Felidar")
    void landfallPutsCounterOnSelf() {
        Permanent felidar = harness.addToBattlefieldAndReturn(player1, new ProwlingFelidar());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(felidar.getEffectivePower()).isEqualTo(3);
        assertThat(felidar.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Prowling Felidar")
    void opponentLandDoesNotTrigger() {
        Permanent felidar = harness.addToBattlefieldAndReturn(player1, new ProwlingFelidar());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(felidar.getEffectivePower()).isEqualTo(2);
        assertThat(felidar.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A land entering without being played triggers only when the ability resolves")
    void landEnteringWithoutBeingPlayedTriggers() {
        Permanent felidar = harness.addToBattlefieldAndReturn(player1, new ProwlingFelidar());

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(felidar.getPlusOnePlusOneCounters()).isZero();
        harness.passBothPriorities();
        assertThat(felidar.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each land entering in the same turn adds another counter")
    void repeatedLandEntriesAccumulateCounters() {
        Permanent felidar = harness.addToBattlefieldAndReturn(player1, new ProwlingFelidar());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(felidar.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Prowling Felidar puts its landfall counter on itself")
    void multipleFelidarsEachReceiveTheirOwnCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ProwlingFelidar());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new ProwlingFelidar());

        assertThat(first.getPlusOnePlusOneCounters()).isZero();
        assertThat(second.getPlusOnePlusOneCounters()).isZero();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(second.getPlusOnePlusOneCounters()).isEqualTo(1);
    }
}
