package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaveWingElemental.class, Forest.class})
class WaveWingElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Wave-Wing Elemental +2/+2 until end of turn")
    void landfallBoostsWaveWingElemental() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WaveWingElemental());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(5);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Wave-Wing Elemental")
    void opponentLandDoesNotTrigger() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WaveWingElemental());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(3);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WaveWingElemental());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(elemental.getEffectivePower()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(3);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Lands entering without being played trigger landfall and their boosts accumulate")
    void multipleLandEntriesAccumulate() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WaveWingElemental());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(elemental.getEffectivePower()).isEqualTo(3);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(5);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(6);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(7);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Each Elemental receives only its own landfall boost")
    void multipleElementalsBoostThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WaveWingElemental());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new WaveWingElemental());

        assertThat(gd.stack).isEmpty();
        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(5);
        assertThat(first.getEffectiveToughness()).isEqualTo(6);
        assertThat(second.getEffectivePower()).isEqualTo(5);
        assertThat(second.getEffectiveToughness()).isEqualTo(6);
    }
}
