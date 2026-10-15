package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderDrake.class, LightningBolt.class})
class ThunderDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when you cast your second spell each turn")
    void putsCounterOnSecondSpell() {
        Permanent drake = addCreatureReady(player1, new ThunderDrake());
        int initialPower = drake.getEffectivePower();
        int initialToughness = drake.getEffectiveToughness();

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(drake.getEffectivePower()).isEqualTo(initialPower);
        assertThat(drake.getEffectiveToughness()).isEqualTo(initialToughness);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(drake.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(drake.getEffectiveToughness()).isEqualTo(initialToughness + 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(drake.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(drake.getEffectiveToughness()).isEqualTo(initialToughness + 1);
    }

    @Test
    void countsItsOwnCastAsTheFirstSpell() {
        harness.setHand(player1, List.of(new ThunderDrake(), new ThunderDrake()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsSpellsDoNotTriggerOrCountTowardControllersSecondSpell() {
        Permanent drake = addCreatureReady(player1, new ThunderDrake());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachDrakeGetsItsOwnCounter() {
        Permanent first = addCreatureReady(player1, new ThunderDrake());
        Permanent second = addCreatureReady(player1, new ThunderDrake());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggerDoesNotPutCounterOnAnotherDrakeAfterSourceDies() {
        Permanent source = addCreatureReady(player1, new ThunderDrake());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        Permanent other = addCreatureReady(player1, new ThunderDrake());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggersAgainOnOpponentsTurnAfterSpellCountResets() {
        Permanent drake = addCreatureReady(player1, new ThunderDrake());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.setLibrary(player2, List.of(new ThunderDrake()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
