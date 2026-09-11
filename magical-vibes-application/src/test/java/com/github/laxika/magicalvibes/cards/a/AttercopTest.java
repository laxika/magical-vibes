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

@CardUsed({Attercop.class, Forest.class})
class AttercopTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when a land you control enters")
    void landfallBoostsAttercop() {
        Permanent attercop = harness.addToBattlefieldAndReturn(player1, new Attercop());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(attercop.getEffectivePower()).isEqualTo(3);
        assertThat(attercop.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's land enters")
    void opponentLandDoesNotTrigger() {
        Permanent attercop = harness.addToBattlefieldAndReturn(player1, new Attercop());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(attercop.getEffectivePower()).isEqualTo(2);
        assertThat(attercop.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent attercop = harness.addToBattlefieldAndReturn(player1, new Attercop());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(attercop.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attercop.getEffectivePower()).isEqualTo(2);
        assertThat(attercop.getEffectiveToughness()).isEqualTo(1);
    }
}
