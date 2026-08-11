package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindriderEelTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Windrider Eel +2/+2 until end of turn")
    void landfallBoostsWindriderEel() {
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new WindriderEel());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(eel.getEffectivePower()).isEqualTo(4);
        assertThat(eel.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Windrider Eel")
    void opponentLandDoesNotTrigger() {
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new WindriderEel());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(eel.getEffectivePower()).isEqualTo(2);
        assertThat(eel.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new WindriderEel());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(eel.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(eel.getEffectivePower()).isEqualTo(2);
        assertThat(eel.getEffectiveToughness()).isEqualTo(2);
    }
}
