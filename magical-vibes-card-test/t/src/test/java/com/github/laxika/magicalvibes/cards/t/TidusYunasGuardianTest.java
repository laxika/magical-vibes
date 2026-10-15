package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TidusYunasGuardian.class, Forest.class, GrizzlyBears.class})
class TidusYunasGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, may move a counter between two creatures you control")
    void movesCounterAtBeginningOfCombat() {
        Permanent tidus = addCreatureReady(player1, new TidusYunasGuardian());
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent destination = addCreatureReady(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player1);

        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, destination.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tidus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cheer draws and proliferates when a counter-bearing creature deals combat damage")
    void cheerDrawsAndProliferates() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new TidusYunasGuardian());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(attacker.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cheer does not trigger for a creature without counters")
    void cheerRequiresCounters() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new TidusYunasGuardian());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void mayDeclineMovingCounter() {
        Permanent source = addCreatureReady(player1, new TidusYunasGuardian());
        Permanent destination = addCreatureReady(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, destination.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounters()).isEmpty();
    }

    @Test
    void movingCounterFromCreatureWithoutCountersDoesNothing() {
        Permanent source = addCreatureReady(player1, new TidusYunasGuardian());
        Permanent destination = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, destination.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(source.getCounters()).isEmpty();
        assertThat(destination.getCounters()).isEmpty();
    }

    @Test
    void controllerChoosesWhichKindOfCounterToMove() {
        Permanent source = addCreatureReady(player1, new TidusYunasGuardian());
        Permanent destination = addCreatureReady(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        source.setCounterCount(CounterType.FLYING, 1);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, destination.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(destination.getCounters()).isEmpty();
    }

    @Test
    void decliningCheerOnFirstStrikeAllowsCheerOnRegularDamage() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent attacker = addCreatureReady(player1, new TidusYunasGuardian());
        attacker.setCounterCount(CounterType.DOUBLE_STRIKE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void acceptingCheerOnFirstStrikePreventsAnotherUseOnRegularDamage() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent attacker = addCreatureReady(player1, new TidusYunasGuardian());
        attacker.setCounterCount(CounterType.DOUBLE_STRIKE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cheerBatchesMultipleDealersAndProliferatesEveryKindOnOpponentCreature() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent tidus = addCreatureReady(player1, new TidusYunasGuardian());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        tidus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponent.setCounterCount(CounterType.FLYING, 1);
        tidus.setAttacking(true);
        ally.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(opponent.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.FLYING)).isEqualTo(2);
        assertThat(tidus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }
}
