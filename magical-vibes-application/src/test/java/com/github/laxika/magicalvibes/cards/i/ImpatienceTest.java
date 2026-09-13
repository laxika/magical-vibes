package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Impatience.class, GrizzlyBears.class})
class ImpatienceTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        advanceToEndStep(activePlayer);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("End step deals 2 damage to the active player who didn't cast a spell")
    void dealsDamageWhenNoSpellCast() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);

        advanceToEndStepAndResolve(player1);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("No damage when the active player cast a spell this turn")
    void noDamageWhenSpellCast() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());

        advanceToEndStepAndResolve(player1);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damage is dealt to the end-step player, not Impatience's controller")
    void damageHitsEndStepPlayerNotController() {
        // Opponent controls Impatience; it still burns the active player on their end step.
        harness.addToBattlefield(player2, new Impatience());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStepAndResolve(player1);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No damage if the end-step player casts a spell before the trigger resolves")
    void noDamageWhenSpellCastAfterTrigger() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each player's end step can trigger Impatience")
    void triggersDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStepAndResolve(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
