package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FlailingManticore.class)
class FlailingManticoreTest extends BaseCardTest {

    @Test
    @DisplayName("Any player may pay to give the Manticore +1/+1 until end of turn")
    void anyPlayerMayBoostManticore() {
        Permanent manticore = addCreatureReady(player1, new FlailingManticore());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(manticore.getPowerModifier()).isEqualTo(1);
        assertThat(manticore.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Any player may pay to give the Manticore -1/-1 until end of turn")
    void anyPlayerMayShrinkManticore() {
        Permanent manticore = addCreatureReady(player1, new FlailingManticore());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(manticore.getPowerModifier()).isEqualTo(-1);
        assertThat(manticore.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Power and toughness modifiers wear off at end of turn")
    void modifiersWearOffAtEndOfTurn() {
        Permanent manticore = addCreatureReady(player1, new FlailingManticore());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(manticore.getPowerModifier()).isZero();
        assertThat(manticore.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(manticore.getPowerModifier()).isZero();
        assertThat(manticore.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Multiple activations of the same ability stack")
    void multipleActivationsStack() {
        Permanent manticore = addCreatureReady(player1, new FlailingManticore());
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(manticore.getPowerModifier()).isEqualTo(2);
        assertThat(manticore.getToughnessModifier()).isEqualTo(2);
    }
}
