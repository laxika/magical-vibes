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
}
