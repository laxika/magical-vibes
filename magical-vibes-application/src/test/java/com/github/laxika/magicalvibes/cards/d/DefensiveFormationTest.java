package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefensiveFormationTest extends BaseCardTest {

    @Test
    @DisplayName("Defensive Formation lets the defending player assign an attacker's damage")
    void defendingPlayerAssignsAttackerDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new DefensiveFormation());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).get(0);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        Permanent blocker1 = gd.playerBattlefields.get(player2.getId()).get(1);
        Permanent blocker2 = gd.playerBattlefields.get(player2.getId()).get(2);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
    @DisplayName("Defensive Formation can keep trample damage on a blocker")
    void defendingPlayerCanAssignAllTrampleDamageToBlocker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new DefensiveFormation());
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).get(0);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).get(1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class))
                .isNotNull();
        harness.handleCombatDamageAssigned(player2, 0, Map.of(blocker.getId(), 8));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
