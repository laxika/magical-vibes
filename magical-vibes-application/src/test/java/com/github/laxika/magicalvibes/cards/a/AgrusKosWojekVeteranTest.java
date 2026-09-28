package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.g.GreaterMossdog;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgrusKosWojekVeteran.class, ViashinoFangtail.class, CourierHawk.class, GreaterMossdog.class})
class AgrusKosWojekVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Agrus Kos boosts attacking creatures according to their colors")
    void boostsAttackingCreaturesByColor() {
        Permanent agrus = addCreatureReady(player1, new AgrusKosWojekVeteran());
        Permanent red = addCreatureReady(player1, new ViashinoFangtail());
        Permanent white = addCreatureReady(player1, new CourierHawk());
        Permanent green = addCreatureReady(player1, new GreaterMossdog());

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(agrus.getPowerModifier()).isEqualTo(2);
        assertThat(agrus.getToughnessModifier()).isEqualTo(2);
        assertThat(red.getPowerModifier()).isEqualTo(2);
        assertThat(red.getToughnessModifier()).isZero();
        assertThat(white.getPowerModifier()).isZero();
        assertThat(white.getToughnessModifier()).isEqualTo(2);
        assertThat(green.getPowerModifier()).isZero();
        assertThat(green.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Agrus Kos only boosts attacking creatures")
    void doesNotBoostNonAttackers() {
        addCreatureReady(player1, new AgrusKosWojekVeteran());
        Permanent red = addCreatureReady(player1, new ViashinoFangtail());
        Permanent white = addCreatureReady(player1, new CourierHawk());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(red.getPowerModifier()).isZero();
        assertThat(red.getToughnessModifier()).isZero();
        assertThat(white.getPowerModifier()).isZero();
        assertThat(white.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Agrus Kos does not trigger when another creature attacks")
    void doesNotTriggerWhenAnotherCreatureAttacks() {
        Permanent agrus = addCreatureReady(player1, new AgrusKosWojekVeteran());
        Permanent red = addCreatureReady(player1, new ViashinoFangtail());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(agrus.getPowerModifier()).isZero();
        assertThat(agrus.getToughnessModifier()).isZero();
        assertThat(red.getPowerModifier()).isZero();
        assertThat(red.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Agrus Kos's boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new AgrusKosWojekVeteran());
        Permanent red = addCreatureReady(player1, new ViashinoFangtail());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(red.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(red.getPowerModifier()).isZero();
        assertThat(red.getToughnessModifier()).isZero();
    }
}
