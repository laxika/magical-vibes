package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViciousConquistador.class})
class ViciousConquistadorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking causes each opponent to lose 1 life (plus combat damage)")
    void attackCausesOpponentLifeLoss() {
        addCreatureReady(player1, new ViciousConquistador());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        // Opponent loses 2 total: 1 from trigger + 1 from combat damage (power 1)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Controller does not lose life from own attack trigger")
    void controllerDoesNotLoseLife() {
        addCreatureReady(player1, new ViciousConquistador());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Trigger fires each time it attacks (multiple combats)")
    void triggerFiresEachCombat() {
        addCreatureReady(player1, new ViciousConquistador());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        // First attack: 1 trigger + 1 combat damage = 2
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);

        // Simulate next combat — untap and attack again
        harness.performUntapStep(player1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        // Second attack: another 1 trigger + 1 combat damage = 4 total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Trigger puts an entry on the stack")
    void triggerGoesOnStack() {
        addCreatureReady(player1, new ViciousConquistador());

        declareAttackers(player1, List.of(0));

        // After declaring attackers, the trigger should be on the stack
        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @DisplayName("Opponent's Vicious Conquistador does not cause controller to lose life when opponent attacks")
    void opponentAttackDoesNotAffectController() {
        addCreatureReady(player2, new ViciousConquistador());

        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        // player1 loses 2 life: 1 from trigger + 1 from combat damage (power 1)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore - 2);
        // player2 (controller) should not lose life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore);
    }

    @Test
    @DisplayName("Attack life loss resolves before blockers and combat damage")
    void attackLifeLossResolvesBeforeCombatDamage() {
        addCreatureReady(player1, new ViciousConquistador());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.assertLife(player2, 20);
            assertThat(gd.stack).hasSize(1);

            resolveAllTriggers();

            harness.assertLife(player2, 19);
            harness.assertLife(player1, 20);
        });
    }

    @Test
    @DisplayName("A blocked Conquistador still makes the opponent lose life")
    void blockedAttackerStillCausesLifeLoss() {
        addCreatureReady(player1, new ViciousConquistador());
        addCreatureReady(player2, new ViciousConquistador());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each attacking Conquistador triggers once; a nonattacker does not trigger")
    void onlyAttackingCopiesTrigger() {
        addCreatureReady(player1, new ViciousConquistador());
        addCreatureReady(player1, new ViciousConquistador());
        addCreatureReady(player1, new ViciousConquistador());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            assertThat(gd.stack).hasSize(2);

            resolveAllTriggers();

            harness.assertLife(player2, 18);
            harness.assertLife(player1, 20);
        });
    }
}
