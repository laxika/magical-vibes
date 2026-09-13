package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowstoneCharger.class})
class FlowstoneChargerTest extends BaseCardTest {

    @Test
    void attackingGivesItPlusThreeMinusThreeUntilEndOfTurn() {
        Permanent charger = addCreatureReady(player1, new FlowstoneCharger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(charger.getPowerModifier()).isEqualTo(3);
        assertThat(charger.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    void onlyTheAttackingChargerGetsTheBoost() {
        Permanent attackingCharger = addCreatureReady(player1, new FlowstoneCharger());
        Permanent nonAttackingCharger = addCreatureReady(player1, new FlowstoneCharger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attackingCharger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attackingCharger)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonAttackingCharger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonAttackingCharger)).isEqualTo(5);
    }

    @Test
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent charger = addCreatureReady(player1, new FlowstoneCharger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(charger.getPowerModifier()).isZero();
        assertThat(charger.getToughnessModifier()).isZero();
    }
}
