package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PallidMycoderm.class, GrizzlyBears.class})
class PallidMycodermTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent mycoderm = addMycoderm();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(mycoderm.getCounterCount(CounterType.FUNGUS)).isOne();
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent mycoderm = addMycoderm();
        mycoderm.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mycoderm.getCounterCount(CounterType.FUNGUS)).isOne();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Saproling boosts Funguses but not other creatures")
    void sacrificingSaprolingBoostsFungusesOnly() {
        Permanent mycoderm = addMycoderm();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        mycoderm.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(mycoderm.getPowerModifier()).isOne();
        assertThat(mycoderm.getToughnessModifier()).isOne();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mycoderm.getPowerModifier()).isZero();
        assertThat(mycoderm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost ability requires a Saproling to sacrifice")
    void boostAbilityRequiresSaproling() {
        addMycoderm();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMycoderm() {
        return addCreatureReady(player1, new PallidMycoderm());
    }
}
