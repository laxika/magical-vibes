package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HedronScrabblerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Hedron Scrabbler +1/+1 until end of turn")
    void landfallBoostsHedronScrabbler() {
        Permanent scrabbler = harness.addToBattlefieldAndReturn(player1, new HedronScrabbler());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(scrabbler.getEffectivePower()).isEqualTo(2);
        assertThat(scrabbler.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Hedron Scrabbler")
    void opponentLandDoesNotTrigger() {
        Permanent scrabbler = harness.addToBattlefieldAndReturn(player1, new HedronScrabbler());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(scrabbler.getEffectivePower()).isEqualTo(1);
        assertThat(scrabbler.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent scrabbler = harness.addToBattlefieldAndReturn(player1, new HedronScrabbler());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(scrabbler.getEffectivePower()).isEqualTo(1);
        assertThat(scrabbler.getEffectiveToughness()).isEqualTo(1);
    }
}
