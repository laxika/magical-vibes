package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonsguardEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant puts a +1/+1 counter on Dragonsguard Elite")
    void castingInstantPutsCounterOnElite() {
        Permanent elite = addCreatureReady(player1, new DragonsguardElite());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Copying an instant puts another +1/+1 counter on Dragonsguard Elite")
    void copyingInstantPutsAnotherCounterOnElite() {
        Permanent elite = addCreatureReady(player1, new DragonsguardElite());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability doubles Dragonsguard Elite's +1/+1 counters")
    void activatedAbilityDoublesCounters() {
        Permanent elite = addCreatureReady(player1, new DragonsguardElite());
        elite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
