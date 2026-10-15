package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfectiousHorror.class})
class InfectiousHorrorTest extends BaseCardTest {

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

    @Test
    @DisplayName("Each attacking copy triggers independently, while a nonattacking copy does not")
    void onlyAttackingCopiesTrigger() {
        addCreatureReady(player1, new InfectiousHorror());
        addCreatureReady(player1, new InfectiousHorror());
        addCreatureReady(player1, new InfectiousHorror());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            assertThat(gd.stack).hasSize(2);
            harness.assertLife(player2, 20);
            resolveAllTriggers();
            harness.assertLife(player2, 16);
            harness.assertLife(player1, 20);
        });
    }

    @Test
    @DisplayName("Attack trigger still resolves after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        var horror = addCreatureReady(player1, new InfectiousHorror());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            assertThat(gd.stack).hasSize(1);
            gd.playerBattlefields.get(player1.getId()).remove(horror);
            gd.playerGraveyards.get(player1.getId()).add(horror.getCard());
            resolveAllTriggers();
            harness.assertLife(player2, 18);
            harness.assertLife(player1, 20);
        });
    }

    @Test
    @DisplayName("An opponent's attacking Horror causes its opponent to lose life")
    void opponentControlledHorrorTriggersForItsController() {
        addCreatureReady(player2, new InfectiousHorror());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
            harness.assertLife(player1, 18);
            harness.assertLife(player2, 20);
        });
    }
}
