package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirebirdBlazingRanger.class, GrizzlyBears.class})
class FirebirdBlazingRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives other attacking creatures +Firebird's power/+0")
    void boostsOtherAttackersByItsPower() {
        Permanent firebird = addCreatureReady(player1, new FirebirdBlazingRanger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        firebird.setPowerModifier(2);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(firebird.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not boost itself or non-attacking creatures")
    void onlyBoostsOtherAttackers() {
        Permanent firebird = addCreatureReady(player1, new FirebirdBlazingRanger());
        Permanent attackingBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent homeBear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(firebird.getPowerModifier()).isZero();
        assertThat(attackingBear.getPowerModifier()).isEqualTo(1);
        assertThat(homeBear.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FirebirdBlazingRanger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(bears.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
    }
}
