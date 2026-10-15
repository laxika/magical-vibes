package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeelaSevateemWarrior.class, Forest.class})
class LeelaSevateemWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger for an opponent's first draw in their draw step")
    void doesNotTriggerForFirstDrawInDrawStep() {
        Permanent leela = harness.addToBattlefieldAndReturn(player1, new LeelaSevateemWarrior());
        harness.setLibrary(player2, List.of(new Forest()));
        prepareDrawStep(player2);

        draw(player2);

        assertThat(leela.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on Leela for an additional opponent draw")
    void triggersForAdditionalDrawInDrawStep() {
        Permanent leela = harness.addToBattlefieldAndReturn(player1, new LeelaSevateemWarrior());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        prepareDrawStep(player2);

        draw(player2);
        draw(player2);
        resolveAllTriggers();

        assertThat(leela.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for the controller's draws")
    void doesNotTriggerForControllerDraws() {
        Permanent leela = harness.addToBattlefieldAndReturn(player1, new LeelaSevateemWarrior());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        draw(player1);
        resolveAllTriggers();

        assertThat(leela.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers for every opponent draw outside their draw step")
    void triggersForEveryDrawOutsideDrawStep() {
        Permanent leela = harness.addToBattlefieldAndReturn(player1, new LeelaSevateemWarrior());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        draw(player2);
        draw(player2);

        assertThat(leela.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(leela.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers for an opponent's first draw during someone else's draw step")
    void triggersDuringControllersDrawStep() {
        Permanent leela = harness.addToBattlefieldAndReturn(player1, new LeelaSevateemWarrior());
        harness.setLibrary(player2, List.of(new Forest()));
        prepareDrawStep(player1);

        draw(player2);
        resolveAllTriggers();

        assertThat(leela.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Earlier upkeep draws do not remove the first draw-step draw exception")
    void upkeepDrawDoesNotConsumeDrawStepException() {
        Permanent leela = harness.addToBattlefieldAndReturn(player1, new LeelaSevateemWarrior());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        draw(player2);
        resolveAllTriggers();
        prepareDrawStep(player2);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(leela.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts a first draw-step draw that happened before Leela entered")
    void triggersAfterEnteringFollowingFirstDraw() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        prepareDrawStep(player2);
        draw(player2);
        Permanent leela = harness.addToBattlefieldAndReturn(player1, new LeelaSevateemWarrior());

        draw(player2);
        resolveAllTriggers();

        assertThat(leela.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void prepareDrawStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
