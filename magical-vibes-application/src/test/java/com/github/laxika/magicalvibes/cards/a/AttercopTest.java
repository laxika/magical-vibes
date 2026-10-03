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
    @DisplayName("Landfall uses the stack and triggers for each land entering through an effect")
    void multipleLandEntriesGiveCumulativeBoosts() {
        Permanent attercop = harness.addToBattlefieldAndReturn(player1, new Attercop());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(attercop.getEffectivePower()).isEqualTo(2);
        assertThat(attercop.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(attercop.getEffectivePower()).isEqualTo(3);
        assertThat(attercop.getEffectiveToughness()).isEqualTo(2);

        harness.passBothPriorities();
        assertThat(attercop.getEffectivePower()).isEqualTo(4);
        assertThat(attercop.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Attercop boosts itself and creature entry does not trigger landfall")
    void eachAttercopGetsItsOwnLandfallBoost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Attercop());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new Attercop());
        assertThat(gd.stack).isEmpty();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(2);
        assertThat(second.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);
    }

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
