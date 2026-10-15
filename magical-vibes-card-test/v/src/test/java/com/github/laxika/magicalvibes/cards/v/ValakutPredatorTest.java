package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValakutPredator.class, Forest.class})
class ValakutPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Valakut Predator +2/+2 until end of turn")
    void landfallBoostsSelf() {
        Permanent predator = harness.addToBattlefieldAndReturn(player1, new ValakutPredator());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(predator.getEffectivePower()).isEqualTo(4);
        assertThat(predator.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent predator = harness.addToBattlefieldAndReturn(player1, new ValakutPredator());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(predator.getEffectivePower()).isEqualTo(2);
        assertThat(predator.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Valakut Predator")
    void opponentLandDoesNotTrigger() {
        Permanent predator = harness.addToBattlefieldAndReturn(player1, new ValakutPredator());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(predator.getEffectivePower()).isEqualTo(2);
        assertThat(predator.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Lands entering without being played each trigger a cumulative boost")
    void multipleLandEntriesStack() {
        Permanent predator = harness.addToBattlefieldAndReturn(player1, new ValakutPredator());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(2);
        assertThat(predator.getEffectivePower()).isEqualTo(2);
        assertThat(predator.getEffectiveToughness()).isEqualTo(2);

        resolveAllTriggers();

        assertThat(predator.getEffectivePower()).isEqualTo(6);
        assertThat(predator.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Each Valakut Predator boosts only itself when its controller's land enters")
    void eachPredatorBoostsItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ValakutPredator());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ValakutPredator());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ValakutPredator());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
    }
}
