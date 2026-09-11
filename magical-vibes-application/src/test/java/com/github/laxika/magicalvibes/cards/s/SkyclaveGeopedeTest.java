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

@CardUsed({SkyclaveGeopede.class, Forest.class})
class SkyclaveGeopedeTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Skyclave Geopede +2/+2 until end of turn")
    void landfallBoostsSkyclaveGeopede() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new SkyclaveGeopede());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(5);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Skyclave Geopede")
    void opponentLandDoesNotTrigger() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new SkyclaveGeopede());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new SkyclaveGeopede());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(geopede.getEffectivePower()).isEqualTo(5);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);
    }
}
