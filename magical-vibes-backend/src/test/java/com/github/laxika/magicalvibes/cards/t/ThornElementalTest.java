package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ThornElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Thorn Elemental has AssignCombatDamageAsThoughUnblockedEffect as static effect")
    void hasCorrectStaticEffect() {
        ThornElemental card = new ThornElemental();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(AssignCombatDamageAsThoughUnblockedEffect.class);
    }

    @Test
    @DisplayName("Blocked Thorn Elemental can assign combat damage to defending player")
    void blockedThornElementalAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ThornElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent thornElemental = gd.playerBattlefields.get(player1.getId()).getFirst();
        thornElemental.setSummoningSick(false);
        thornElemental.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance from DECLARE_BLOCKERS → COMBAT_DAMAGE (paused for assignment)
        harness.passBothPriorities();

        // Assign all 7 damage to defending player (as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        // Blocker should survive since no damage was assigned to it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Blocked Thorn Elemental can assign combat damage to blocker instead")
    void blockedThornElementalAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ThornElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent thornElemental = gd.playerBattlefields.get(player1.getId()).getFirst();
        thornElemental.setSummoningSick(false);
        thornElemental.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance from DECLARE_BLOCKERS → COMBAT_DAMAGE (paused for assignment)
        harness.passBothPriorities();

        // Assign all damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        // Grizzly Bears (2/2) takes 7 damage → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Life unchanged since damage went to blocker
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
