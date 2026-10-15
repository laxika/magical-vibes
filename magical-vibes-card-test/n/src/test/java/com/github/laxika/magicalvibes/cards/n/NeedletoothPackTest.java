package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeedletoothPack.class, BearCub.class})
class NeedletoothPackTest extends BaseCardTest {

    @Test
    @DisplayName("Morbid puts two +1/+1 counters on a target creature you control at your end step")
    void morbidPutsTwoCountersOnTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new NeedletoothPack());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new BearCub());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(bears.getId()).doesNotContain(opponentBears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger at your end step when no creature died this turn")
    void doesNotTriggerWithoutMorbid() {
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new NeedletoothPack());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(pack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Fizzling target does not put counters on a creature")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new NeedletoothPack());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BearCub());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, bears.getId());

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself after a creature you control actually dies")
    void canTargetItselfAfterOwnCreatureDies() {
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new NeedletoothPack());
        Permanent cub = harness.addToBattlefieldAndReturn(player1, new BearCub());
        cub.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Bear Cub");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, pack.getId());
        resolveAllTriggers();

        assertThat(pack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step even with morbid")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new NeedletoothPack());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(pack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature dying after the end step begins does not trigger the ability")
    void deathDuringEndStepIsTooLate() {
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new NeedletoothPack());
        Permanent cub = harness.addToBattlefieldAndReturn(player2, new BearCub());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        cub.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Bear Cub");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(pack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A target that changes to the opponent's control is illegal on resolution")
    void targetMustStillBeControlledOnResolution() {
        harness.addToBattlefield(player1, new NeedletoothPack());
        Permanent cub = harness.addToBattlefieldAndReturn(player1, new BearCub());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, cub.getId());
        gd.playerBattlefields.get(player1.getId()).remove(cub);
        gd.playerBattlefields.get(player2.getId()).add(cub);
        resolveAllTriggers();

        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability still resolves if Needletooth Pack dies after triggering")
    void resolvesAfterSourceDies() {
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new NeedletoothPack());
        Permanent cub = harness.addToBattlefieldAndReturn(player1, new BearCub());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, cub.getId());
        pack.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Needletooth Pack");
        resolveAllTriggers();

        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
