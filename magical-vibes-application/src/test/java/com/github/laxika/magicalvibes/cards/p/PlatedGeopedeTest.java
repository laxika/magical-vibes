package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlatedGeopedeTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Plated Geopede +2/+2 until end of turn")
    void landfallBoostsPlatedGeopede() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new PlatedGeopede());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Plated Geopede")
    void opponentLandDoesNotTrigger() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new PlatedGeopede());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(1);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new PlatedGeopede());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(1);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);
    }
}
