package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SporeFlowerTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent flower = addFlower();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(flower.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing three spore counters prevents all combat damage this turn")
    void removesThreeSporeCountersAndPreventsCombatDamage() {
        Permanent flower = addFlower();
        flower.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flower.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("The prevention ability requires three spore counters")
    void preventionAbilityRequiresThreeSporeCounters() {
        Permanent flower = addFlower();
        flower.setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addFlower() {
        Permanent flower = harness.addToBattlefieldAndReturn(player1, new SporeFlower());
        flower.setSummoningSick(false);
        return flower;
    }
}
