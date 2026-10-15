package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IcatianJavelineers;
import com.github.laxika.magicalvibes.cards.i.IcatianPriest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SporeFlower.class, IcatianPriest.class, IcatianJavelineers.class})
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

    @Test
    @DisplayName("Spore counters are paid immediately and only three are removed")
    void countersArePaidBeforeResolution() {
        Permanent flower = addFlower();
        flower.setCounterCount(CounterType.FUNGUS, 5);

        harness.activateAbility(player1, 0, null, null);

        assertThat(flower.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
        assertThat(gd.preventAllCombatDamage).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(flower.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Spore Flower can activate its prevention ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent flower = addFlower();
        flower.setSummoningSick(true);
        flower.tap();
        flower.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flower.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @CardUsed(IcatianPriest.class)
    @DisplayName("The activated ability resolves even after Spore Flower leaves the battlefield")
    void preventionResolvesWithoutSource() {
        Permanent flower = addFlower();
        flower.setCounterCount(CounterType.FUNGUS, 3);
        addCreatureReady(player2, new IcatianPriest());

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(flower);
        gd.playerGraveyards.get(player1.getId()).add(flower.getCard());
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed(IcatianPriest.class)
    @DisplayName("Combat damage is prevented for attacking and blocking creatures")
    void preventsCombatDamageToBothCreatures() {
        Permanent flower = addFlower();
        flower.setCounterCount(CounterType.FUNGUS, 3);
        Permanent attacker = addCreatureReady(player1, new IcatianPriest());
        Permanent blocker = addCreatureReady(player2, new IcatianPriest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    private Permanent addFlower() {
        return addCreatureReady(player1, new SporeFlower());
    }
}
