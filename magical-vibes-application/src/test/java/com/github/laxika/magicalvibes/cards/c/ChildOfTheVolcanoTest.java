package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PromisingVein;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChildOfTheVolcano.class, Forest.class, PromisingVein.class})
class ChildOfTheVolcanoTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself at your end step after descending")
    void putsCounterAfterDescending() {
        Permanent child = harness.addToBattlefieldAndReturn(player1, new ChildOfTheVolcano());
        descendThisTurn();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself at your end step without descending")
    void doesNotPutCounterWithoutDescending() {
        Permanent child = harness.addToBattlefieldAndReturn(player1, new ChildOfTheVolcano());

        advanceToEndStep(player1);

        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple descents give only one counter at the end step")
    void multipleDescentsGiveOneCounter() {
        Permanent child = harness.addToBattlefieldAndReturn(player1, new ChildOfTheVolcano());
        descendThisTurn();
        descendThisTurn();

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Descending before this creature enters still counts")
    void descentBeforeEnteringCounts() {
        descendThisTurn();
        Permanent child = harness.addToBattlefieldAndReturn(player1, new ChildOfTheVolcano());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step even after descending")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent child = harness.addToBattlefieldAndReturn(player1, new ChildOfTheVolcano());
        descendThisTurn();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent descending does not satisfy your end-step condition")
    void opponentsDescentDoesNotCount() {
        Permanent child = harness.addToBattlefieldAndReturn(player1, new ChildOfTheVolcano());
        descendThisTurn(player2);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Descending after the end step begins does not create a trigger")
    void descentAfterEndStepBeginsDoesNotTrigger() {
        Permanent child = harness.addToBattlefieldAndReturn(player1, new ChildOfTheVolcano());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        descendThisTurn();

        assertThat(gd.stack).isEmpty();
        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void descendThisTurn() {
        descendThisTurn(player1);
    }

    private void descendThisTurn(Player player) {
        int veinIndex = gd.playerBattlefields.get(player.getId()).size();
        harness.addToBattlefield(player, new PromisingVein());
        harness.setLibrary(player, List.of(new Forest()));
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.activateAbility(player, veinIndex, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player, 0);
        harness.assertInGraveyard(player, "Promising Vein");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
