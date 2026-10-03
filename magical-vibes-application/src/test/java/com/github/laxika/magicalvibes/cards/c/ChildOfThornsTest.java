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

    @Test
    @DisplayName("A tapped, summoning-sick Child can activate during the opponent's turn")
    void activatesWithoutTapOrSummoningSicknessRestriction() {
        Permanent child = harness.addToBattlefieldAndReturn(player1, new ChildOfThorns());
        child.setSummoningSick(true);
        child.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(child);
        harness.assertInGraveyard(player1, "Child of Thorns");
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Child can target itself but is sacrificed before the ability resolves")
    void canTargetItself() {
        Permanent child = harness.addToBattlefieldAndReturn(player1, new ChildOfThorns());

        harness.activateAbility(player1, 0, null, child.getId());

        harness.assertNotOnBattlefield(player1, "Child of Thorns");
        harness.assertInGraveyard(player1, "Child of Thorns");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(child.getPowerModifier()).isZero();
        assertThat(child.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability does not boost a target sacrificed in response")
    void targetSacrificedInResponse() {
        harness.addToBattlefield(player1, new ChildOfThorns());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChildOfThorns());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new GnarledMass());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 0, null, survivor.getId());
        harness.assertInGraveyard(player2, "Child of Thorns");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(survivor.getPowerModifier()).isEqualTo(1);
        assertThat(survivor.getToughnessModifier()).isEqualTo(1);
    }
}
