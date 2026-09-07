package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerimeterSergeant.class, EliteVanguard.class, GrizzlyBears.class})
class PerimeterSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Perimeter Sergeant boosts other Humans you control by +1/+0")
    void attackBoostsOtherHumans() {
        addCreatureReady(player1, new PerimeterSergeant());
        Permanent human = addCreatureReady(player1, new EliteVanguard());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(human.getPowerModifier()).isEqualTo(1);
        assertThat(human.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack trigger excludes Perimeter Sergeant itself and non-Humans")
    void attackExcludesSelfAndNonHumans() {
        Permanent sergeant = addCreatureReady(player1, new PerimeterSergeant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(sergeant.getPowerModifier()).isZero();
        assertThat(sergeant.getToughnessModifier()).isZero();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The +1/+0 boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new PerimeterSergeant());
        Permanent human = addCreatureReady(player1, new EliteVanguard());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(human.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(human.getPowerModifier()).isZero();
    }
}
