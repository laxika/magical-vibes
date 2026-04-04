package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViciousConquistadorTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has EachOpponentLosesLifeEffect(1) on ON_ATTACK")
    void hasAttackTrigger() {
        ViciousConquistador card = new ViciousConquistador();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(EachOpponentLosesLifeEffect.class);
        EachOpponentLosesLifeEffect effect =
                (EachOpponentLosesLifeEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== ON_ATTACK — each opponent loses 1 life =====

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
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        Permanent conquistador = gd.playerBattlefields.get(player1.getId()).getFirst();
        conquistador.untap();

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

    // ===== Helper methods =====


    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
