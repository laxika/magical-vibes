package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootSnare.class, GreenwoodSentinel.class, Shock.class})
class RootSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage this turn")
    void preventsAllCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RootSnare()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        Permanent attacker = addCreatureReady(player1, new GreenwoodSentinel());
        attacker.setAttacking(true);
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents damage from both attackers and blockers, including unblocked attackers")
    void preventsDamageToCreaturesAndDefendingPlayer() {
        Permanent attacker = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent unblockedAttacker = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent blocker = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player2, List.of(new RootSnare()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        declareAttackers(player1, List.of(0, 1));
        harness.castAndResolveInstant(player2, 0);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.assertLife(player2, 20);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(unblockedAttacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(attacker, unblockedAttacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(blocker);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage to players or creatures")
    void doesNotPreventNoncombatDamage() {
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new RootSnare(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Combat damage resumes on the following turn")
    void preventionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new RootSnare()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addCreatureReady(player2, new GreenwoodSentinel());
        addCreatureReady(player1, new GreenwoodSentinel());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }
}
