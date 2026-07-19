package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfectiousHorrorTest extends BaseCardTest {

    // ===== ON_ATTACK — each opponent loses 2 life =====

    @Test
    @DisplayName("Attacking causes each opponent to lose 2 life (plus combat damage)")
    void attackCausesOpponentLifeLoss() {
        addCreatureReady(player1, new InfectiousHorror());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        // Opponent loses 4 total: 2 from trigger + 2 from combat damage (power 2)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Controller does not lose life from own attack trigger")
    void controllerDoesNotLoseLife() {
        addCreatureReady(player1, new InfectiousHorror());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Trigger puts an entry on the stack")
    void triggerGoesOnStack() {
        addCreatureReady(player1, new InfectiousHorror());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isNotEmpty();
    }

    // ===== Helper methods =====

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
