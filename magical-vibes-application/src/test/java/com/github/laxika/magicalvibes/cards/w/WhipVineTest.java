package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FyndhornDruid;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhipVine.class, WildAesthir.class, FyndhornDruid.class})
class WhipVineTest extends BaseCardTest {

    @Test
    @DisplayName("The ability taps the flying creature Whip Vine blocks")
    void abilityTapsBlockedFlyer() {
        Permanent bird = addCreatureReady(player1, new WildAesthir());
        addCreatureReady(player2, new WhipVine());

        blockWithVine();
        bird.untap(); // isolate the tap from the attack tap
        harness.activateAbility(player2, 0, null, bird.getId());
        harness.passBothPriorities();

        assertThat(bird.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The locked creature does not untap while Whip Vine remains tapped")
    void lockedCreatureDoesNotUntap() {
        Permanent bird = addCreatureReady(player1, new WildAesthir());
        Permanent vine = addCreatureReady(player2, new WhipVine());

        blockWithVine();
        harness.activateAbility(player2, 0, null, bird.getId());
        harness.passBothPriorities();
        assertThat(vine.isTapped()).isTrue();

        advanceToNextTurnWithMayChoice(player1, false); // player2's untap: Whip Vine stays tapped
        advanceToNextTurn(player2); // player1's untap step

        assertThat(vine.isTapped()).isTrue();
        assertThat(bird.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The locked creature untaps once Whip Vine untaps")
    void lockedCreatureUntapsWhenVineUntaps() {
        Permanent bird = addCreatureReady(player1, new WildAesthir());
        Permanent vine = addCreatureReady(player2, new WhipVine());

        blockWithVine();
        harness.activateAbility(player2, 0, null, bird.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player1, true); // player2's untap: Whip Vine untaps
        assertThat(vine.isTapped()).isFalse();

        advanceToNextTurn(player2); // player1's untap step

        assertThat(bird.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a blocked creature without flying")
    void cannotTargetNonFlyingBlockedCreature() {
        Permanent druid = addCreatureReady(player1, new FyndhornDruid());
        addCreatureReady(player2, new WhipVine());

        blockWithVine();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, druid.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a flying creature Whip Vine isn't blocking")
    void cannotTargetUnblockedFlyer() {
        addCreatureReady(player1, new WildAesthir());
        Permanent otherBird = addCreatureReady(player1, new WildAesthir());
        addCreatureReady(player2, new WhipVine());
        otherBird.setAttacking(true);

        blockWithVine();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, otherBird.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetFlyerBlockedByAnotherCreature() {
        Permanent bird = addCreatureReady(player1, new WildAesthir());
        addCreatureReady(player2, new WhipVine());
        addCreatureReady(player2, new WildAesthir());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, bird.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapRestrictionDoesNotResumeAfterVineIsRetapped() {
        Permanent bird = addCreatureReady(player1, new WildAesthir());
        Permanent vine = addCreatureReady(player2, new WhipVine());

        blockWithVine();
        harness.activateAbility(player2, 0, null, bird.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player1, true);
        assertThat(vine.isTapped()).isFalse();

        vine.tap();
        advanceToNextTurn(player2);

        assertThat(bird.isTapped()).isFalse();
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
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);

        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
