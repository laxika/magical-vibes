package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Malignus;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndergrowthChampion.class, Forest.class, Shock.class, Malignus.class})
class UndergrowthChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on Undergrowth Champion")
    void landfallPutsCounterOnSelf() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new UndergrowthChampion());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage is prevented and removes only one +1/+1 counter while countered")
    void damageIsPreventedAndRemovesOneCounter() {
        Permanent champion = harness.addToBattlefieldAndReturn(player2, new UndergrowthChampion());
        champion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, champion.getId());

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(champion.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Undergrowth Champion");
    }

    @Test
    @DisplayName("Damage is dealt normally without a +1/+1 counter")
    void damageIsNotPreventedWithoutCounter() {
        Permanent champion = harness.addToBattlefieldAndReturn(player2, new UndergrowthChampion());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, champion.getId());

        harness.assertNotOnBattlefield(player2, "Undergrowth Champion");
        harness.assertInGraveyard(player2, "Undergrowth Champion");
    }

    @Test
    @DisplayName("Opponent's land does not trigger landfall")
    void opponentsLandDoesNotPutCounterOnSelf() {
        Permanent champion = harness.addToBattlefieldAndReturn(player2, new UndergrowthChampion());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Landfall counter is added only when its trigger resolves")
    void landfallDoesNotProtectBeforeTriggerResolves() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new UndergrowthChampion());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.playLand(player1, 0);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castAndResolveInstant(player2, 0, champion.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Undergrowth Champion");
        harness.assertInGraveyard(player1, "Undergrowth Champion");
    }

    @Test
    @DisplayName("Last counter prevents the whole damage event but not a later one")
    void lastCounterPreventsOnlyFirstDamageEvent() {
        Permanent champion = harness.addToBattlefieldAndReturn(player2, new UndergrowthChampion());
        champion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, champion.getId());

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(champion.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Undergrowth Champion");

        harness.castAndResolveInstant(player1, 0, champion.getId());

        harness.assertNotOnBattlefield(player2, "Undergrowth Champion");
        harness.assertInGraveyard(player2, "Undergrowth Champion");
    }

    @Test
    @DisplayName("Simultaneous combat damage from two blockers removes only one counter")
    void simultaneousCombatDamageRemovesOneCounter() {
        Permanent champion = addCreatureReady(player1, new UndergrowthChampion());
        champion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent first = addCreatureReady(player2, new UndergrowthChampion());
        Permanent second = addCreatureReady(player2, new UndergrowthChampion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(first.getId(), 3, second.getId(), 2));

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(champion.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Undergrowth Champion");
    }

    @Test
    @DisplayName("Unpreventable combat damage still removes one counter")
    void unpreventableCombatDamageStillRemovesCounter() {
        harness.setLife(player2, 4);
        addCreatureReady(player1, new Malignus());
        Permanent champion = addCreatureReady(player2, new UndergrowthChampion());
        champion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(champion.getMarkedDamage()).isEqualTo(2);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Undergrowth Champion");
    }
}
