package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

class WhipVineTest extends BaseCardTest {

    @Test
    @DisplayName("The ability taps the flying creature Whip Vine blocks")
    void abilityTapsBlockedFlyer() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        addCreatureReady(player2, new WhipVine());

        blockWithVine();
        hawk.untap(); // isolate the tap from the attack tap
        harness.activateAbility(player2, 0, null, hawk.getId());
        harness.passBothPriorities();

        assertThat(hawk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The locked creature does not untap while Whip Vine remains tapped")
    void lockedCreatureDoesNotUntap() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        Permanent vine = addCreatureReady(player2, new WhipVine());

        blockWithVine();
        harness.activateAbility(player2, 0, null, hawk.getId());
        harness.passBothPriorities();
        assertThat(vine.isTapped()).isTrue();

        advanceToNextTurnWithMayChoice(player1, false); // player2's untap: Whip Vine stays tapped
        advanceToNextTurn(player2); // player1's untap step

        assertThat(vine.isTapped()).isTrue();
        assertThat(hawk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The locked creature untaps once Whip Vine untaps")
    void lockedCreatureUntapsWhenVineUntaps() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        Permanent vine = addCreatureReady(player2, new WhipVine());

        blockWithVine();
        harness.activateAbility(player2, 0, null, hawk.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player1, true); // player2's untap: Whip Vine untaps
        assertThat(vine.isTapped()).isFalse();

        advanceToNextTurn(player2); // player1's untap step

        assertThat(hawk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a blocked creature without flying")
    void cannotTargetNonFlyingBlockedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new WhipVine());

        blockWithVine();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a flying creature Whip Vine isn't blocking")
    void cannotTargetUnblockedFlyer() {
        addCreatureReady(player1, new SuntailHawk());
        Permanent otherHawk = addCreatureReady(player1, new SuntailHawk());
        addCreatureReady(player2, new WhipVine());
        otherHawk.setAttacking(true);

        blockWithVine();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, otherHawk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    /** Declares player1's first creature as an attacker and blocks it with player2's Whip Vine. */
    private void blockWithVine() {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
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
