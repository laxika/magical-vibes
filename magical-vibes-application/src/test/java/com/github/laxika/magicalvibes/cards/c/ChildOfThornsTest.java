package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChildOfThorns.class, GnarledMass.class, GodsEyeGateToTheReikai.class})
class ChildOfThornsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and gives target creature +1/+1 until end of turn")
    void sacrificesAndBoostsTargetCreature() {
        Permanent child = addCreatureReady(player1, new ChildOfThorns());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(child);
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new ChildOfThorns());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GodsEyeGateToTheReikai());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ChildOfThorns());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }
}
