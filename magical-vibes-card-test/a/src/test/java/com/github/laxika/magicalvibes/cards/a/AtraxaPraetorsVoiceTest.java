package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtraxaPraetorsVoice.class, GrizzlyBears.class})
class AtraxaPraetorsVoiceTest extends BaseCardTest {

    @Test
    @DisplayName("Proliferates at the beginning of your end step")
    void proliferatesAtControllerEndStep() {
        harness.addToBattlefield(player1, new AtraxaPraetorsVoice());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not proliferate at an opponent's end step")
    void doesNotProliferateAtOpponentEndStep() {
        harness.addToBattlefield(player1, new AtraxaPraetorsVoice());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed({AtraxaPraetorsVoice.class})
    void canChooseNoPermanentsOrPlayers() {
        Permanent atraxa = harness.addToBattlefieldAndReturn(player1, new AtraxaPraetorsVoice());
        atraxa.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        resolveEndStepTrigger();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(atraxa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @CardUsed({AtraxaPraetorsVoice.class})
    void addsEachExistingCounterKindToSelectedOpposingPermanentAndPlayer() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AtraxaPraetorsVoice());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new AtraxaPraetorsVoice());
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opposing.setCounterCount(CounterType.CHARGE, 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerRadCounters.put(player2.getId(), 3);

        resolveEndStepTrigger();
        harness.handleMultiplePermanentsChosen(player1, List.of(opposing.getId(), player2.getId()));

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opposing.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(opposing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @CardUsed({AtraxaPraetorsVoice.class})
    void resolvesWithoutAChoiceWhenNobodyHasCounters() {
        harness.addToBattlefield(player1, new AtraxaPraetorsVoice());

        resolveEndStepTrigger();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({AtraxaPraetorsVoice.class})
    void canProliferatePlayerWithOnlyEnergyCounters() {
        harness.addToBattlefield(player1, new AtraxaPraetorsVoice());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        resolveEndStepTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    @CardUsed({AtraxaPraetorsVoice.class})
    void addsEveryCounterKindToSelectedPlayer() {
        harness.addToBattlefield(player1, new AtraxaPraetorsVoice());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerExperienceCounters.put(player1.getId(), 3);

        resolveEndStepTrigger();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    @CardUsed({AtraxaPraetorsVoice.class})
    void canSelectMultiplePermanentsAndPlayersInOneProliferation() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AtraxaPraetorsVoice());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new AtraxaPraetorsVoice());
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposing.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        resolveEndStepTrigger();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(own.getId(), opposing.getId(), player1.getId(), player2.getId()));

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @CardUsed({AtraxaPraetorsVoice.class})
    void triggeredAbilityStillProliferatesAfterAtraxaLeavesBattlefield() {
        Permanent atraxa = harness.addToBattlefieldAndReturn(player1, new AtraxaPraetorsVoice());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(atraxa);
        gd.playerGraveyards.get(player1.getId()).add(atraxa.getCard());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
