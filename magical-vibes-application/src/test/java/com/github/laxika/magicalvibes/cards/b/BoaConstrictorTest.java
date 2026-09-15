package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BoaConstrictor.class)
class BoaConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +3/+3 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(constrictor.getPowerModifier()).isEqualTo(3);
        assertThat(constrictor.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability taps Boa Constrictor")
    void abilityTapsConstrictor() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());

        harness.activateAbility(player1, 0, null, null);

        assertThat(constrictor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(constrictor.getPowerModifier()).isZero();
        assertThat(constrictor.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate while Boa Constrictor has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BoaConstrictor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate while Boa Constrictor is already tapped")
    void cannotActivateWhileTapped() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());
        constrictor.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
