package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemperedSliver.class, MetallicSliver.class, GrizzlyBears.class})
class TemperedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Tempered Sliver puts a +1/+1 counter on itself after dealing combat damage")
    void putsCounterOnItself() {
        Permanent sliver = addAttackingCreature(player1, new TemperedSliver());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tempered Sliver grants the combat-damage trigger to other Slivers you control")
    void grantsTriggerToOtherControlledSlivers() {
        harness.addToBattlefield(player1, new TemperedSliver());
        Permanent sliver = addAttackingCreature(player1, new MetallicSliver());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tempered Sliver does not grant the trigger to non-Slivers or opposing Slivers")
    void onlyAffectsControlledSlivers() {
        harness.addToBattlefield(player1, new TemperedSliver());
        Permanent nonSliver = addAttackingCreature(player1, new GrizzlyBears());
        Permanent opposingSliver = addAttackingCreature(player2, new MetallicSliver());

        resolveCombat(player1);
        harness.passBothPriorities();
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(nonSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
