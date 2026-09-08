package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IcatianJavelineers;
import com.github.laxika.magicalvibes.cards.i.IcatianPriest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SporeFlower.class)
class SporeFlowerTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent flower = addFlower();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(flower.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during an opponent's upkeep")
    void upkeepTriggerOnlyFiresDuringControllerUpkeep() {
        Permanent flower = addFlower();

        advanceToUpkeep(player2);

        assertThat(flower.getCounterCount(CounterType.FUNGUS)).isZero();
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
    @CardUsed(IcatianPriest.class)
    @DisplayName("Removing three spore counters prevents combat damage from reaching a player")
    void preventsCombatDamageFromReachingPlayer() {
        Permanent flower = addFlower();
        flower.setCounterCount(CounterType.FUNGUS, 3);
        addCreatureReady(player1, new IcatianPriest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed(IcatianJavelineers.class)
    @DisplayName("Combat damage prevention does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent flower = addFlower();
        flower.setCounterCount(CounterType.FUNGUS, 3);
        Permanent javelineers = addCreatureReady(player1, new IcatianJavelineers());
        javelineers.setCounterCount(CounterType.JAVELIN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
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
        return addCreatureReady(player1, new SporeFlower());
    }
}
