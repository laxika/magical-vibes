package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EndlessWurm;
import com.github.laxika.magicalvibes.cards.p.PouncingJaguar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefensiveFormation.class, PouncingJaguar.class})
class DefensiveFormationTest extends BaseCardTest {

    @Test
    @DisplayName("Defensive Formation lets the defending player assign an attacker's damage")
    void defendingPlayerAssignsAttackerDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new DefensiveFormation());
        Permanent attacker = addCreatureReady(player1, new PouncingJaguar());
        Permanent blocker1 = addCreatureReady(player2, new PouncingJaguar());
        Permanent blocker2 = addCreatureReady(player2, new PouncingJaguar());

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        resolveCombat(player1);

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(
                player1, 0, Map.of(blocker1.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(blocker2.getId(), 2));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker2.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Defensive Formation gives the defending player each attacking creature's assignment")
    void defendingPlayerAssignsEachAttackerDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new DefensiveFormation());
        Permanent attacker1 = addCreatureReady(player1, new PouncingJaguar());
        Permanent attacker2 = addCreatureReady(player1, new PouncingJaguar());
        Permanent blocker1 = addCreatureReady(player2, new PouncingJaguar());
        Permanent blocker2 = addCreatureReady(player2, new PouncingJaguar());
        Permanent blocker3 = addCreatureReady(player2, new PouncingJaguar());
        Permanent blocker4 = addCreatureReady(player2, new PouncingJaguar());

        attacker1.setAttacking(true);
        attacker1.setAttackTarget(player2.getId());
        attacker2.setAttacking(true);
        attacker2.setAttackTarget(player2.getId());

        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);
        blocker3.setBlocking(true);
        blocker3.addBlockingTarget(1);
        blocker4.setBlocking(true);
        blocker4.addBlockingTarget(1);

        resolveCombat(player1);

        PendingInteraction.CombatDamageAssignment firstPrompt = gd.interaction
                .activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(firstPrompt).isNotNull();
        assertThat(firstPrompt.playerId()).isEqualTo(player2.getId());
        assertThat(firstPrompt.attackerIndex()).isEqualTo(0);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(blocker2.getId(), 2));

        PendingInteraction.CombatDamageAssignment secondPrompt = gd.interaction
                .activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(secondPrompt).isNotNull();
        assertThat(secondPrompt.playerId()).isEqualTo(player2.getId());
        assertThat(secondPrompt.attackerIndex()).isEqualTo(1);
        harness.handleCombatDamageAssigned(player2, 1, Map.of(blocker4.getId(), 2));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker3.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker4.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed(EndlessWurm.class)
    @DisplayName("Defensive Formation can keep trample damage on a blocker")
    void defendingPlayerCanAssignAllTrampleDamageToBlocker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new DefensiveFormation());
        Permanent attacker = addCreatureReady(player1, new EndlessWurm());
        Permanent blocker = addCreatureReady(player2, new PouncingJaguar());

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        PendingInteraction.CombatDamageAssignment prompt = gd.interaction
                .activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        harness.handleCombatDamageAssigned(player2, 0, Map.of(blocker.getId(), 9));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
