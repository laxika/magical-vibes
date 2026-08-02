package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThalakosDreamsowerTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    /** Advance from the given active player's turn into the next player's turn. */
    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }

    /** Same, answering the new active player's may-not-untap prompt with {@code acceptUntap}. */
    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP -> advanceTurn -> may ability prompt

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }

    @Test
    @DisplayName("Damage to an opponent prompts a choice restricted to creatures")
    void promptsToChooseCreature() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent enemyLand = addPermanent(player2, new Forest());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(enemyCreature.getId())
                .doesNotContain(enemyLand.getId());
    }

    @Test
    @DisplayName("The chosen creature becomes tapped")
    void tapsChosenCreature() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(enemyCreature.getId()));

        assertThat(enemyCreature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The tapped creature stays tapped while Thalakos Dreamsower remains tapped, and untaps once it untaps")
    void untapLockLastsWhileSourceTapped() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(enemyCreature.getId()));
        dreamsower.tap();

        // Player 2's untap step — the locked creature stays tapped.
        advanceToNextTurn(player1);
        assertThat(enemyCreature.isTapped()).isTrue();

        // Player 1's untap step — choose to untap Thalakos Dreamsower, releasing the lock.
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(dreamsower.isTapped()).isFalse();

        // Player 2's next untap step — the creature untaps now.
        advanceToNextTurn(player1);
        assertThat(enemyCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller may choose not to untap Thalakos Dreamsower during their untap step")
    void mayChooseNotToUntap() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.tap();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(dreamsower.isTapped()).isTrue();
    }
}
