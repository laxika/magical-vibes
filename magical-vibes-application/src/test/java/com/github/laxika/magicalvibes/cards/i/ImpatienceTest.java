package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpatienceTest extends BaseCardTest {

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto stack
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("End step deals 2 damage to the active player who didn't cast a spell")
    void dealsDamageWhenNoSpellCast() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("No damage when the active player cast a spell this turn")
    void noDamageWhenSpellCast() {
        harness.addToBattlefield(player1, new Impatience());
        harness.setLife(player1, 20);
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage is dealt to the end-step player, not Impatience's controller")
    void damageHitsEndStepPlayerNotController() {
        // Opponent controls Impatience; it still burns the active player on their end step.
        harness.addToBattlefield(player2, new Impatience());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
