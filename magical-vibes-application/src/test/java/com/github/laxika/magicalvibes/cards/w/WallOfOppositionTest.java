package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WallOfOpposition.class)
class WallOfOppositionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Wall of Opposition +1/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent wall = addCreatureReady(player1, new WallOfOpposition());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(1);
        assertThat(wall.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Repeated activations give a cumulative boost")
    void repeatedActivationsStack() {
        Permanent wall = addCreatureReady(player1, new WallOfOpposition());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(2);
        assertThat(wall.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent wall = addCreatureReady(player1, new WallOfOpposition());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isZero();
        assertThat(wall.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability requires one generic mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new WallOfOpposition());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Colored mana can pay for the ability while the wall is tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfOpposition());
        wall.setSummoningSick(true);
        wall.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(1);
        assertThat(wall.getToughnessModifier()).isZero();
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost applies only to the wall that activated it and waits for resolution")
    void boostsOnlyItsSourceOnResolution() {
        Permanent firstWall = addCreatureReady(player1, new WallOfOpposition());
        Permanent secondWall = addCreatureReady(player1, new WallOfOpposition());
        Permanent opposingWall = addCreatureReady(player2, new WallOfOpposition());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(secondWall.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(firstWall.getPowerModifier()).isZero();
        assertThat(secondWall.getPowerModifier()).isEqualTo(1);
        assertThat(opposingWall.getPowerModifier()).isZero();
    }
}
