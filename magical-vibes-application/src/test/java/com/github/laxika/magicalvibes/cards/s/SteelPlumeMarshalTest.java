package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteelPlumeMarshal.class, WindDrake.class, GrizzlyBears.class})
class SteelPlumeMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other attacking creatures you control with flying")
    void boostsOtherAttackingCreaturesWithFlying() {
        Permanent marshal = addCreatureReady(player1, new SteelPlumeMarshal());
        Permanent attackingDrake = addCreatureReady(player1, new WindDrake());
        Permanent unblockedDrake = addCreatureReady(player1, new WindDrake());
        Permanent attackingBears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 3));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, attackingDrake)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attackingDrake)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, unblockedDrake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unblockedDrake)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attackingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attackingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SteelPlumeMarshal());
        Permanent drake = addCreatureReady(player1, new WindDrake());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
    }
}
