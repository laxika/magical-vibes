package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IcecaveCrasher.class, Forest.class})
class IcecaveCrasherTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Icecave Crasher +1/+0 until end of turn")
    void landfallBoostsIcecaveCrasher() {
        Permanent crasher = harness.addToBattlefieldAndReturn(player1, new IcecaveCrasher());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(crasher.getEffectivePower()).isEqualTo(5);
        assertThat(crasher.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Icecave Crasher")
    void opponentLandDoesNotTrigger() {
        Permanent crasher = harness.addToBattlefieldAndReturn(player1, new IcecaveCrasher());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(crasher.getEffectivePower()).isEqualTo(4);
        assertThat(crasher.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent crasher = harness.addToBattlefieldAndReturn(player1, new IcecaveCrasher());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crasher.getEffectivePower()).isEqualTo(4);
        assertThat(crasher.getEffectiveToughness()).isEqualTo(4);
    }
}
