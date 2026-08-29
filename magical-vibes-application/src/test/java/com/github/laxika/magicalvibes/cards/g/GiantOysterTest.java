package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AysenAbbey;
import com.github.laxika.magicalvibes.cards.l.LabyrinthMinotaur;
import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantOyster.class, WillowFaerie.class, AysenAbbey.class, LabyrinthMinotaur.class})
class GiantOysterTest extends BaseCardTest {

    @Test
    @DisplayName("The ability cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        addOyster(player1);
        Permanent creature = addCreature(player2, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("The ability cannot target a tapped noncreature permanent")
    void cannotTargetTappedNoncreature() {
        addOyster(player1);
        Permanent land = addLand(player2);
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("The locked creature does not untap while Giant Oyster remains tapped")
    void lockedCreatureDoesNotUntap() {
        Permanent oyster = addOyster(player1);
        Permanent creature = lockCreature(oyster);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The locked creature untaps once Giant Oyster untaps")
    void lockedCreatureUntapsWhenOysterUntaps() {
        Permanent oyster = addOyster(player1);
        Permanent creature = lockCreature(oyster);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(oyster.isTapped()).isFalse();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each of the controller's draw steps puts a -1/-1 counter on the locked creature")
    void drawStepPutsMinusOneCounterOnLockedCreature() {
        Permanent oyster = addOyster(player1);
        Permanent creature = lockCreature(oyster);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw-step trigger

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No -1/-1 counter is placed when Giant Oyster locks nothing")
    void drawStepPlacesNoCounterWithoutLock() {
        addOyster(player1);
        Permanent creature = addCreature(player2, true);

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's draw step does not put a counter on the locked creature")
    void opponentDrawStepPlacesNoCounter() {
        Permanent oyster = addOyster(player1);
        Permanent creature = lockCreature(oyster);

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A draw-step counter trigger resolves after Giant Oyster untaps")
    void drawStepTriggerResolvesAfterOysterUntaps() {
        Permanent oyster = addOyster(player1);
        Permanent creature = lockCreature(oyster);

        advanceToDraw(player1);
        harness.performUntapStep(player1);
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(PlayerInputService.class).processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(oyster.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A draw-step counter trigger resolves after Giant Oyster leaves the battlefield")
    void drawStepTriggerResolvesAfterOysterLeavesBattlefield() {
        Permanent oyster = addOyster(player1);
        Permanent creature = lockCreature(oyster);

        advanceToDraw(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, oyster));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untapping Giant Oyster removes all -1/-1 counters from the locked creature")
    void untappingOysterRemovesCounters() {
        Permanent oyster = addOyster(player1);
        Permanent creature = lockCreature(oyster);

        advanceToDraw(player1);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(oyster.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining to untap Giant Oyster keeps the lock active")
    void decliningToUntapKeepsLockActive() {
        Permanent oyster = addOyster(player1);
        Permanent creature = lockCreature(oyster);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, false);
        assertThat(oyster.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapping Giant Oyster removes all existing -1/-1 counters from the locked creature")
    void untappingOysterRemovesExistingCountersToo() {
        Permanent oyster = addOyster(player1);
        Permanent creature = addMinotaur(player2, true);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        advanceToDraw(player1);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Giant Oyster leaving the battlefield removes all -1/-1 counters from the locked creature")
    void oysterLeavingBattlefieldRemovesCounters() {
        Permanent oyster = addOyster(player1);
        Permanent creature = lockCreature(oyster);

        advanceToDraw(player1);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, oyster));

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private Permanent addOyster(Player player) {
        return addCreatureReady(player, new GiantOyster());
    }

    private Permanent addCreature(Player player, boolean tapped) {
        Permanent perm = addCreatureReady(player, new WillowFaerie());
        if (tapped) {
            perm.tap();
        }
        return perm;
    }

    private Permanent addMinotaur(Player player, boolean tapped) {
        Permanent perm = addCreatureReady(player, new LabyrinthMinotaur());
        if (tapped) {
            perm.tap();
        }
        return perm;
    }

    private Permanent addLand(Player player) {
        Permanent land = new Permanent(new AysenAbbey());
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }

    /** Taps the Oyster to lock a tapped creature that player2 controls, and resolves the ability. */
    private Permanent lockCreature(Permanent oyster) {
        Permanent creature = addCreature(player2, true);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(oyster.isTapped()).isTrue();
        return creature;
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip / empty-library loss
        harness.setLibrary(activePlayer, List.of(new WillowFaerie()));
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.DRAW); // fires the draw-step trigger
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
