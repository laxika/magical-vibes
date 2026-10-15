package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FanaticOfXenagos.class})
class FanaticOfXenagosTest extends BaseCardTest {

    @Test
    @DisplayName("Paying tribute puts a +1/+1 counter on Fanatic of Xenagos")
    void tributePaid() {
        Permanent fanatic = castFanatic();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(fanatic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, fanatic)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, fanatic, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining tribute gives Fanatic of Xenagos +1/+1 and haste until end of turn")
    void tributeNotPaidBoostsAndGivesHaste() {
        Permanent fanatic = castFanatic();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(fanatic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, fanatic)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fanatic)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, fanatic, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The unpaid tribute bonus expires at end of turn")
    void tributeNotPaidBonusExpires() {
        Permanent fanatic = castFanatic();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fanatic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fanatic)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, fanatic, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Paid tribute counters persist after the turn ends")
    void paidTributeCounterPersists() {
        Permanent fanatic = castFanatic();
        harness.handleMayAbilityChosen(player2, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fanatic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, fanatic)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fanatic)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, fanatic, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The other player chooses tribute when player two controls Fanatic")
    void tributeChoiceUsesOpposingPlayer() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new FanaticOfXenagos(), "{1}{R}{G}");
        harness.passBothPriorities();
        Permanent fanatic = findPermanent(player2, "Fanatic of Xenagos");

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(fanatic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, fanatic)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fanatic)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, fanatic, Keyword.HASTE)).isTrue();
    }

    private Permanent castFanatic() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FanaticOfXenagos(), "{1}{R}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Fanatic of Xenagos");
    }
}
