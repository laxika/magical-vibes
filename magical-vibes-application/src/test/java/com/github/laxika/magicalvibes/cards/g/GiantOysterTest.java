package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiantOysterTest extends BaseCardTest {

    @Test
    @DisplayName("The ability cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        addOyster(player1);
        Permanent angel = addAngel(player2, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("The locked creature does not untap while Giant Oyster remains tapped")
    void lockedCreatureDoesNotUntap() {
        Permanent oyster = addOyster(player1);
        Permanent angel = lockAngel(oyster);

        advanceToNextTurn(player1);

        assertThat(angel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The locked creature untaps once Giant Oyster untaps")
    void lockedCreatureUntapsWhenOysterUntaps() {
        Permanent oyster = addOyster(player1);
        Permanent angel = lockAngel(oyster);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(oyster.isTapped()).isFalse();

        advanceToNextTurn(player1);

        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each of the controller's draw steps puts a -1/-1 counter on the locked creature")
    void drawStepPutsMinusOneCounterOnLockedCreature() {
        Permanent oyster = addOyster(player1);
        Permanent angel = lockAngel(oyster);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw-step trigger

        assertThat(angel.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No -1/-1 counter is placed when Giant Oyster locks nothing")
    void drawStepPlacesNoCounterWithoutLock() {
        addOyster(player1);
        Permanent angel = addAngel(player2, true);

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(angel.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Untapping Giant Oyster removes all -1/-1 counters from the locked creature")
    void untappingOysterRemovesCounters() {
        Permanent oyster = addOyster(player1);
        Permanent angel = lockAngel(oyster);

        advanceToDraw(player1);
        harness.passBothPriorities();
        assertThat(angel.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(oyster.isTapped()).isFalse();
        assertThat(angel.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Giant Oyster leaving the battlefield removes all -1/-1 counters from the locked creature")
    void oysterLeavingBattlefieldRemovesCounters() {
        Permanent oyster = addOyster(player1);
        Permanent angel = lockAngel(oyster);

        advanceToDraw(player1);
        harness.passBothPriorities();
        assertThat(angel.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, oyster));

        assertThat(angel.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private Permanent addOyster(Player player) {
        Permanent perm = new Permanent(new GiantOyster());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAngel(Player player, boolean tapped) {
        Permanent perm = new Permanent(new SerraAngel());
        perm.setSummoningSick(false);
        if (tapped) {
            perm.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    /** Taps the Oyster to lock a tapped Serra Angel that player2 controls, and resolves the ability. */
    private Permanent lockAngel(Permanent oyster) {
        Permanent angel = addAngel(player2, true);
        harness.activateAbility(player1, 0, null, angel.getId());
        harness.passBothPriorities();
        assertThat(oyster.isTapped()).isTrue();
        return angel;
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip / empty-library loss
        harness.setLibrary(activePlayer, List.of(new SerraAngel()));
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UPKEEP -> DRAW, fires the draw-step trigger
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (untap)
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // cascades into the next turn's may-not-untap prompt

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
