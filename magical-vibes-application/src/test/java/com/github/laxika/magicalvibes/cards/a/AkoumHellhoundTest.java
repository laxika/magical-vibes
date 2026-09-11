package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkoumHellhound.class, Forest.class})
class AkoumHellhoundTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Akoum Hellhound +2/+2 until end of turn")
    void landfallBoostsAkoumHellhound() {
        Permanent hellhound = harness.addToBattlefieldAndReturn(player1, new AkoumHellhound());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(hellhound.getEffectivePower()).isEqualTo(2);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Akoum Hellhound")
    void opponentLandDoesNotTrigger() {
        Permanent hellhound = harness.addToBattlefieldAndReturn(player1, new AkoumHellhound());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(hellhound.getEffectivePower()).isZero();
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent hellhound = harness.addToBattlefieldAndReturn(player1, new AkoumHellhound());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hellhound.getEffectivePower()).isZero();
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(1);
    }
}
