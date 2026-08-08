package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.c.CaressOfPhyrexia;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SuncleanserTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 1: remove all counters from target creature and lock it")
    class CreatureMode {

        @Test
        @DisplayName("Removes every counter from the targeted creature")
        void removesAllCounters() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            bears.setCounterCount(CounterType.CHARGE, 1);

            castWithCreatureMode(bears.getId());
            harness.passBothPriorities(); // resolve Suncleanser
            harness.passBothPriorities(); // resolve the ETB trigger

            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(bears.getCounterCount(CounterType.CHARGE)).isZero();
        }

        @Test
        @DisplayName("The locked creature can't have counters put on it afterwards")
        void lockedCreatureGetsNoNewCounters() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            castWithCreatureMode(bears.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new BurstOfStrength()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.castInstant(player1, 0, bears.getId());
            harness.passBothPriorities();

            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }

        @Test
        @DisplayName("The lock ends when Suncleanser leaves the battlefield")
        void lockEndsWhenSuncleanserLeaves() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            castWithCreatureMode(bears.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            Permanent suncleanser = findPermanent(player1, "Suncleanser");
            harness.setHand(player2, List.of(new Murder()));
            harness.addMana(player2, ManaColor.BLACK, 3);
            harness.castInstant(player2, 0, suncleanser.getId());
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new BurstOfStrength()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.castInstant(player1, 0, bears.getId());
            harness.passBothPriorities();

            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        private void castWithCreatureMode(UUID targetId) {
            harness.setHand(player1, List.of(new Suncleanser()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.castCreature(player1, 0, 0, targetId);
        }
    }

    @Nested
    @DisplayName("Mode 2: target opponent loses all counters and is locked")
    class PlayerMode {

        @Test
        @DisplayName("Clears the targeted opponent's poison counters")
        void clearsPoisonCounters() {
            gd.playerPoisonCounters.put(player2.getId(), 3);

            castWithPlayerMode();
            harness.passBothPriorities(); // resolve Suncleanser
            harness.passBothPriorities(); // resolve the ETB trigger

            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        @DisplayName("The locked opponent can't get poison counters afterwards")
        void lockedPlayerGetsNoNewCounters() {
            castWithPlayerMode();
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new CaressOfPhyrexia()));
            harness.addMana(player1, ManaColor.BLACK, 5);
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        @DisplayName("The controller is unaffected by the opponent's lock")
        void controllerStillGetsCounters() {
            castWithPlayerMode();
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new CaressOfPhyrexia()));
            harness.addMana(player1, ManaColor.BLACK, 5);
            harness.castInstant(player1, 0, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(3);
        }

        private void castWithPlayerMode() {
            harness.setHand(player1, List.of(new Suncleanser()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.castCreature(player1, 0, 1, player2.getId());
        }
    }
}
