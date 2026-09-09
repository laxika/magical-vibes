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

@CardUsed({ScytheLeopard.class, Forest.class})
class ScytheLeopardTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Scythe Leopard +1/+1 until end of turn")
    void landfallBoostsScytheLeopard() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(leopard.getEffectivePower()).isEqualTo(2);
        assertThat(leopard.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Scythe Leopard")
    void opponentLandDoesNotTrigger() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(leopard.getEffectivePower()).isEqualTo(1);
        assertThat(leopard.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(leopard.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(leopard.getEffectivePower()).isEqualTo(1);
        assertThat(leopard.getEffectiveToughness()).isEqualTo(1);
    }
}
