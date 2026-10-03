package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blightbeetle.class, GrizzlyBears.class, BurstOfStrength.class, SerraAngel.class,
        Skinrender.class, BarkhideTroll.class, TurnToFrog.class})
class BlightbeetleTest extends BaseCardTest {

    @Test
    void opponentsCreaturesCannotGetPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new Blightbeetle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, bearsId);

        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void controllerCreaturesCanGetPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new Blightbeetle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, bearsId);

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void otherCounterTypesRemainAllowed() {
        harness.addToBattlefield(player1, new Blightbeetle());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0, angel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(angel.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    void protectionFromGreenPreventsGreenSpellsFromTargetingIt() {
        Permanent blightbeetle = harness.addToBattlefieldAndReturn(player1, new Blightbeetle());
        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, blightbeetle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsCreaturesEnterWithoutPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new Blightbeetle());
        Permanent troll = harness.enterBattlefieldAndReturn(player2, new BarkhideTroll());

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Barkhide Troll");
    }

    @Test
    void controllerCreaturesStillEnterWithPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new Blightbeetle());
        Permanent troll = harness.enterBattlefieldAndReturn(player1, new BarkhideTroll());

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void existingCountersRemainWhenBlightbeetleEnters() {
        Permanent troll = harness.enterBattlefieldAndReturn(player2, new BarkhideTroll());
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, new Blightbeetle());
        harness.runStateBasedActions();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void removingOpposingCreaturesAbilitiesDoesNotRemoveCounterRestriction() {
        harness.addToBattlefield(player1, new Blightbeetle());
        Permanent troll = harness.addToBattlefieldAndReturn(player2, new BarkhideTroll());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, troll.getId());

        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, troll.getId());

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void removingBlightbeetlesAbilitiesAllowsOpposingCreaturesToReceiveCounters() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new Blightbeetle());
        Permanent troll = harness.addToBattlefieldAndReturn(player2, new BarkhideTroll());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, beetle.getId());

        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, troll.getId());

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opposingCreaturesCanStillReceiveMinusOneMinusOneCounters() {
        harness.addToBattlefield(player1, new Blightbeetle());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, angel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(angel.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    void greenCreaturesCannotBlockBlightbeetle() {
        Permanent beetle = addCreatureReady(player1, new Blightbeetle());
        beetle.setAttacking(true);
        addCreatureReady(player2, new BarkhideTroll());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void blightbeetleCanBlockGreenCreaturesAndPreventsTheirDamage() {
        Permanent troll = addCreatureReady(player1, new BarkhideTroll());
        troll.setAttacking(true);
        addCreatureReady(player2, new Blightbeetle());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertOnBattlefield(player2, "Blightbeetle");
        harness.assertOnBattlefield(player1, "Barkhide Troll");
        harness.assertLife(player2, 20);
    }
}
