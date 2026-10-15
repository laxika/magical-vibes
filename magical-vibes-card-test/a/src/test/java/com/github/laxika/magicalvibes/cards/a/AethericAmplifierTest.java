package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AethericAmplifier.class, GrizzlyBears.class})
class AethericAmplifierTest extends BaseCardTest {

    @Test
    void doublesEachKindOfCounterOnTargetPermanent() {
        Permanent amplifier = addReadyAmplifier();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 3);
        addFourMana();

        harness.activateAbility(player1, 0, 1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        assertThat(amplifier.isTapped()).isTrue();
    }

    @Test
    void doublesEachKindOfCounterTheControllerHas() {
        addReadyAmplifier();
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerRadCounters.put(player1.getId(), 1);
        gd.playerSparkCounters.put(player1.getId(), 4);
        gd.playerExperienceCounters.put(player1.getId(), 5);
        addFourMana();

        harness.activateAbility(player1, 0, 1, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(6);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerSparkCounters.get(player1.getId())).isEqualTo(8);
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(10);
    }

    @Test
    void permanentModeRejectsAPlayerTarget() {
        addReadyAmplifier();
        addFourMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAmplifier() {
        Permanent amplifier = harness.addToBattlefieldAndReturn(player1, new AethericAmplifier());
        amplifier.setSummoningSick(false);
        return amplifier;
    }

    private void addFourMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
