package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MachineManModelX51.class, Shock.class})
class MachineManModelX51Test extends BaseCardTest {

    @Test
    void castingANoncreatureSpellAddsACounterAndGrantsFlying() {
        Permanent machineMan = addReadyMachineMan();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(machineMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, machineMan, Keyword.FLYING)).isTrue();
    }

    @Test
    void castingACreatureSpellDoesNotTriggerMachineMan() {
        Permanent machineMan = addReadyMachineMan();
        harness.setHand(player1, List.of(new MachineManModelX51()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);

        assertThat(machineMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, machineMan, Keyword.FLYING)).isFalse();
    }

    @Test
    void flyingGrantedByTheTriggerWearsOffAtEndOfTurn() {
        Permanent machineMan = addReadyMachineMan();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gqs.hasKeyword(gd, machineMan, Keyword.FLYING)).isTrue();

        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, machineMan, Keyword.FLYING)).isFalse();
        assertThat(machineMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerMachineMan() {
        Permanent machineMan = addReadyMachineMan();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(machineMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, machineMan, Keyword.FLYING)).isFalse();
    }

    @Test
    void eachNoncreatureSpellAddsAnotherCounter() {
        Permanent machineMan = addReadyMachineMan();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(machineMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, machineMan, Keyword.FLYING)).isTrue();
    }

    @Test
    void triggerResolvesBeforeTheNoncreatureSpell() {
        Permanent machineMan = addReadyMachineMan();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int initialLife = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());

        assertThat(machineMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, machineMan, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(machineMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, machineMan, Keyword.FLYING)).isTrue();
        harness.assertLife(player2, initialLife);

        harness.passBothPriorities();
        harness.assertLife(player2, initialLife - 2);
    }

    private Permanent addReadyMachineMan() {
        return addCreatureReady(player1, new MachineManModelX51());
    }
}
