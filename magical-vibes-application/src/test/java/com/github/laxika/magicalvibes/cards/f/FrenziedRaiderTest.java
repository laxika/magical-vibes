package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BattershieldWarrior;
import com.github.laxika.magicalvibes.cards.w.WaterServant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrenziedRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you activate a boast ability, Frenzied Raider gets a +1/+1 counter")
    void getsCounterWhenBoastAbilityIsActivated() {
        Permanent raider = addCreatureReady(player1, new FrenziedRaider());
        Permanent warrior = addCreatureReady(player1, new BattershieldWarrior());
        warrior.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(raider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Frenzied Raider does not trigger for a non-boast ability")
    void doesNotTriggerForNonBoastAbility() {
        Permanent raider = addCreatureReady(player1, new FrenziedRaider());
        addCreatureReady(player1, new WaterServant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(raider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
