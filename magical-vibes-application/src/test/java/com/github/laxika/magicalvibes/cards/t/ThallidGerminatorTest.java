package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({ThallidGerminator.class, GrizzlyBears.class})
class ThallidGerminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent thallid = addThallid();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Saproling boosts a creature")
    void sacrificingSaprolingBoostsTargetCreature() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The token ability requires three spore counters")
    void tokenAbilityRequiresThreeSporeCounters() {
        addThallid().setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost ability cannot sacrifice a non-Saproling creature")
    void boostAbilityRequiresSaproling() {
        addThallid();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off during cleanup")
    void boostWearsOffAtEndOfTurn() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private Permanent addThallid() {
        Permanent thallid = harness.addToBattlefieldAndReturn(player1, new ThallidGerminator());
        thallid.setSummoningSick(false);
        return thallid;
    }
}
