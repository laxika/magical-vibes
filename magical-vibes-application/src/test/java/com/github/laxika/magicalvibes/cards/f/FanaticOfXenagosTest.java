package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent castFanatic() {
        harness.setHand(player1, java.util.List.of(new FanaticOfXenagos()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Fanatic of Xenagos");
    }
}
