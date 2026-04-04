package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderPoisonedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChainedThroatseekerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC CantAttackUnlessDefenderPoisonedEffect")
    void hasCorrectStructure() {
        ChainedThroatseeker card = new ChainedThroatseeker();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantAttackUnlessDefenderPoisonedEffect.class);
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Can attack when defending player is poisoned")
    void canAttackWhenDefenderPoisoned() {
        harness.setLife(player2, 20);
        Permanent perm = addCreatureReady(player1, new ChainedThroatseeker());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        declareAttackers(player1, List.of(0));

        // Attack went through — defender gets poison counters from infect (not life loss)
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isGreaterThan(1);
    }

    @Test
    @DisplayName("Cannot attack when defending player has no poison counters")
    void cannotAttackWhenDefenderNotPoisoned() {
        Permanent perm = addCreatureReady(player1, new ChainedThroatseeker());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only the controller is poisoned")
    void cannotAttackWhenOnlyControllerPoisoned() {
        Permanent perm = addCreatureReady(player1, new ChainedThroatseeker());
        gd.playerPoisonCounters.put(player1.getId(), 5);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when defender has multiple poison counters")
    void canAttackWhenDefenderHasMultiplePoisonCounters() {
        harness.setLife(player2, 20);
        Permanent perm = addCreatureReady(player1, new ChainedThroatseeker());
        gd.playerPoisonCounters.put(player2.getId(), 7);

        declareAttackers(player1, List.of(0));

        // Attack went through — poison counters increased by 5 (power)
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(12);
    }

    // ===== Combat damage (infect) =====

    @Test
    @DisplayName("Deals damage to players as poison counters (infect)")
    void dealsPoisonCountersToPlayers() {
        harness.setLife(player2, 20);
        Permanent perm = addCreatureReady(player1, new ChainedThroatseeker());
        perm.setAttacking(true);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Infect deals poison counters instead of life loss
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(6); // 1 existing + 5 from combat
    }

    // ===== Helper methods =====


    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
