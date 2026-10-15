package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed({LakeshoreApothecary.class})
class LakeshoreApothecaryTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn puts a +1/+1 counter on Lakeshore Apothecary")
    void secondDrawAddsCounterOnlyOnce() {
        Permanent apothecary = harness.addToBattlefieldAndReturn(player1, new LakeshoreApothecary());
        harness.setLibrary(player1, List.of(new LakeshoreApothecary(), new LakeshoreApothecary(), new LakeshoreApothecary()));

        drawCard();
        assertThat(apothecary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        drawCard();
        assertThat(gd.stack).hasSize(1);
        drawCard();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(apothecary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second draw also triggers during an opponent's turn")
    void secondDrawOnOpponentTurnAddsCounter() {
        harness.forceActivePlayer(player2);
        Permanent apothecary = harness.addToBattlefieldAndReturn(player1, new LakeshoreApothecary());
        harness.setLibrary(player1, List.of(new LakeshoreApothecary(), new LakeshoreApothecary()));

        drawCard();
        drawCard();
        assertThat(apothecary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(apothecary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the drawing player's Apothecary triggers")
    void opponentDrawsDoNotTriggerOwnApothecary() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new LakeshoreApothecary());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new LakeshoreApothecary());
        harness.setLibrary(player2, List.of(new LakeshoreApothecary(), new LakeshoreApothecary()));

        drawCard(player2);
        assertThat(gd.stack).isEmpty();
        drawCard(player2);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A draw before Apothecary entered still counts toward the second draw")
    void earlierDrawBeforeEnteringCounts() {
        harness.setLibrary(player1, List.of(new LakeshoreApothecary(), new LakeshoreApothecary()));
        drawCard();
        Permanent apothecary = harness.addToBattlefieldAndReturn(player1, new LakeshoreApothecary());

        drawCard();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(apothecary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger on a third draw")
    void enteringAfterSecondDrawDoesNotTrigger() {
        harness.setLibrary(player1, List.of(new LakeshoreApothecary(), new LakeshoreApothecary(), new LakeshoreApothecary()));
        drawCard();
        drawCard();
        Permanent apothecary = harness.addToBattlefieldAndReturn(player1, new LakeshoreApothecary());

        drawCard();

        assertThat(gd.stack).isEmpty();
        assertThat(apothecary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A departed Apothecary's trigger cannot put a counter on another Apothecary")
    void departedSourceDoesNotPutCounterOnReplacement() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new LakeshoreApothecary());
        harness.setLibrary(player1, List.of(new LakeshoreApothecary(), new LakeshoreApothecary()));
        drawCard();
        drawCard();
        gd.playerBattlefields.get(player1.getId()).remove(original);
        harness.setGraveyard(player1, List.of(original.getCard()));
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new LakeshoreApothecary());

        resolveAllTriggers();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second draw can trigger again in the next turn")
    void nextTurnResetsDrawCount() {
        harness.forceActivePlayer(player1);
        Permanent apothecary = harness.addToBattlefieldAndReturn(player1, new LakeshoreApothecary());
        harness.setLibrary(player1, List.of(new LakeshoreApothecary(), new LakeshoreApothecary(),
                new LakeshoreApothecary(), new LakeshoreApothecary()));
        drawCard();
        drawCard();
        resolveAllTriggers();
        assertThat(apothecary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        drawCard();
        assertThat(gd.stack).isEmpty();
        drawCard();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(apothecary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Vigilance allows Apothecary to attack without tapping")
    void attackingDoesNotTap() {
        Permanent apothecary = harness.addToBattlefieldAndReturn(player1, new LakeshoreApothecary());
        apothecary.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(apothecary.isAttacking()).isTrue();
        assertThat(apothecary.isTapped()).isFalse();
    }

    private void drawCard() {
        drawCard(player1);
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
