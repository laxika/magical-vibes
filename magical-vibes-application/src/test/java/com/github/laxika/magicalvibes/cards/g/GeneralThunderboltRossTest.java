package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeneralThunderboltRoss.class, GrizzlyBears.class})
class GeneralThunderboltRossTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion gives attacking creatures +1/+0")
    void battalionBoostsAttackers() {
        Permanent ross = addCreatureReady(player1, new GeneralThunderboltRoss());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(ross.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(otherAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(nonAttacker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        Permanent ross = addCreatureReady(player1, new GeneralThunderboltRoss());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(ross.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Battalion boost wears off at end of turn")
    void battalionBoostWearsOff() {
        Permanent ross = addCreatureReady(player1, new GeneralThunderboltRoss());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(ross.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ross.getPowerModifier()).isZero();
    }
}
