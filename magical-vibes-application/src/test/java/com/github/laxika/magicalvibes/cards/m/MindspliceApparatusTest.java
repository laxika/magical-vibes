package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindspliceApparatusTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an oil counter on itself at the beginning of your upkeep")
    void addsOilCounterAtUpkeep() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(apparatus.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reduces your instant and sorcery spells by the number of oil counters")
    void reducesInstantAndSorcerySpells() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());
        apparatus.setCounterCount(CounterType.OIL, 2);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce creature spells")
    void doesNotReduceCreatureSpells() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new MindspliceApparatus());
        apparatus.setCounterCount(CounterType.OIL, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
