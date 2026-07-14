package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BellowingFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 to the damaged creature's controller and 3 to you on combat damage to a creature")
    void damagesBothControllersOnCombatDamageToCreature() {
        Permanent fiend = addCreatureReady(player1, new BellowingFiend());
        fiend.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        blockAttacker(player2, new GrizzlyBears(), 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage — Fiend deals 3 to the blocker
        harness.passBothPriorities(); // resolve the triggered ability

        // 3 damage to the blocker's controller (player2) and 3 damage to Bellowing Fiend's controller (player1).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not trigger when it deals combat damage to a player")
    void doesNotTriggerOnDamageToPlayer() {
        Permanent fiend = addCreatureReady(player1, new BellowingFiend());
        fiend.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // unblocked — 3 combat damage to player2, no creature damaged

        // Only combat damage to the face; the trigger never fires, so player1 loses no life.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when another creature you control deals damage to a creature")
    void doesNotTriggerForOtherCreatures() {
        addCreatureReady(player1, new BellowingFiend());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        blockAttacker(player2, new GrizzlyBears(), 1); // block the Grizzly Bears (attacker index 1)

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage — Grizzly Bears deals 2 to the blocker
        harness.passBothPriorities();

        // The Fiend is not the damage source, so neither player takes the punisher damage.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    /** Adds {@code blockerCard} to {@code blocker}'s battlefield blocking the attacker at {@code attackerIndex}. */
    private void blockAttacker(Player blocker, Card blockerCard, int attackerIndex) {
        Permanent perm = new Permanent(blockerCard);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        perm.addBlockingTarget(attackerIndex);
        gd.playerBattlefields.get(blocker.getId()).add(perm);
    }
}
