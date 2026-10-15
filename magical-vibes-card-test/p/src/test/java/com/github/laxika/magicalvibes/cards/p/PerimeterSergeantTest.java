package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.c.CheckpointOfficer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerimeterSergeant.class, CheckpointOfficer.class, AlmightyBrushwagg.class})
class PerimeterSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Perimeter Sergeant boosts other Humans you control by +1/+0")
    void attackBoostsOtherHumans() {
        addCreatureReady(player1, new PerimeterSergeant());
        Permanent human = addCreatureReady(player1, new CheckpointOfficer());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(human.getPowerModifier()).isEqualTo(1);
        assertThat(human.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack trigger excludes Perimeter Sergeant itself and non-Humans")
    void attackExcludesSelfAndNonHumans() {
        Permanent sergeant = addCreatureReady(player1, new PerimeterSergeant());
        Permanent nonHuman = addCreatureReady(player1, new AlmightyBrushwagg());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(sergeant.getPowerModifier()).isZero();
        assertThat(sergeant.getToughnessModifier()).isZero();
        assertThat(nonHuman.getPowerModifier()).isZero();
        assertThat(nonHuman.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The +1/+0 boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new PerimeterSergeant());
        Permanent human = addCreatureReady(player1, new CheckpointOfficer());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(human.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(human.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The attack trigger does not boost opposing Humans")
    void attackDoesNotBoostOpposingHumans() {
        addCreatureReady(player1, new PerimeterSergeant());
        Permanent ownHuman = addCreatureReady(player1, new CheckpointOfficer());
        Permanent opposingHuman = addCreatureReady(player2, new CheckpointOfficer());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ownHuman.getPowerModifier()).isEqualTo(1);
        assertThat(opposingHuman.getPowerModifier()).isZero();
        assertThat(opposingHuman.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Two attacking Sergeants boost each other and stack their boosts on other Humans")
    void multipleSergeantsBoostEachOther() {
        Permanent first = addCreatureReady(player1, new PerimeterSergeant());
        Permanent second = addCreatureReady(player1, new PerimeterSergeant());
        Permanent human = addCreatureReady(player1, new CheckpointOfficer());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(human.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(human.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack trigger boosts Humans present at resolution but not later arrivals")
    void boostUsesHumansPresentAtResolution() {
        addCreatureReady(player1, new PerimeterSergeant());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = addCreatureReady(player1, new CheckpointOfficer());
        resolveAllTriggers();
        Permanent afterResolution = addCreatureReady(player1, new CheckpointOfficer());

        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isZero();
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }
}
