package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.f.FieldTrip;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonsguardElite.class, GiantGrowth.class, GrizzlyBears.class, BarkshellBlessing.class})
class DragonsguardEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant puts a +1/+1 counter on Dragonsguard Elite")
    void castingInstantPutsCounterOnElite() {
        Permanent elite = addCreatureReady(player1, new DragonsguardElite());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

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
        harness.handleMayAbilityChosen(player1, false);
        for (int i = 0; i < 6 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

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

    @Test
    @CardUsed({FieldTrip.class})
    void castingSorceryAddsCounterBeforeSpellResolves() {
        Permanent elite = addCreatureReady(player1, new DragonsguardElite());
        harness.setHand(player1, List.of(new FieldTrip()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingCreatureDoesNotAddCounter() {
        Permanent elite = addCreatureReady(player1, new DragonsguardElite());
        harness.setHand(player1, List.of(new DragonsguardElite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsInstantDoesNotAddCounter() {
        Permanent elite = addCreatureReady(player1, new DragonsguardElite());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, elite.getId());

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doublingZeroCountersDoesNothing() {
        Permanent elite = addCreatureReady(player1, new DragonsguardElite());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doublingIncludesMagecraftCounterAddedInResponse() {
        Permanent elite = addCreatureReady(player1, new DragonsguardElite());
        elite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player1, 0, elite.getId());
        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @CardUsed({DoublingSeason.class})
    void doublingAppliesCounterReplacementEffects() {
        Permanent elite = addCreatureReady(player1, new DragonsguardElite());
        elite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }
}
