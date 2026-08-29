package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SabSunenLuxaEmbodiedTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack with an even number of counters, including zero")
    void canAttackWithEvenCounterCount() {
        addCreatureReady(player1, new SabSunenLuxaEmbodied());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack with an odd number of counters")
    void cannotAttackWithOddCounterCount() {
        Permanent sabSunen = addSabSunen(player1);
        sabSunen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot block with an odd number of counters")
    void cannotBlockWithOddCounterCount() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent sabSunen = addSabSunen(player1);
        sabSunen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Adds a counter and draws two cards when the new total is odd")
    void addsCounterAndDrawsWhenNewTotalIsOdd() {
        Permanent sabSunen = addSabSunen(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(sabSunen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Adds a counter without drawing when the new total is even")
    void addsCounterWithoutDrawingWhenNewTotalIsEven() {
        Permanent sabSunen = addSabSunen(player1);
        sabSunen.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(sabSunen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's first main phase")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent sabSunen = addSabSunen(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(sabSunen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addSabSunen(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SabSunenLuxaEmbodied());
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
