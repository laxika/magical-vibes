package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SteppeLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Steppe Lynx +2/+2 until end of turn")
    void landfallBoostsSteppeLynx() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(lynx.getEffectivePower()).isEqualTo(2);
        assertThat(lynx.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Steppe Lynx")
    void opponentLandDoesNotTrigger() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(lynx.getEffectivePower()).isEqualTo(0);
        assertThat(lynx.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(lynx.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lynx.getEffectivePower()).isEqualTo(0);
        assertThat(lynx.getEffectiveToughness()).isEqualTo(1);
    }
}
