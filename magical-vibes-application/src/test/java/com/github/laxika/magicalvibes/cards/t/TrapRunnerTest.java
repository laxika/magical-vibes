package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BlockadeRunner;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrapRunner.class, FreshVolunteers.class, BlockadeRunner.class})
class TrapRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Makes an unblocked attacker blocked with no blocker")
    void makesUnblockedAttackerBlocked() {
        Permanent trapRunner = addCreatureReady(player1, new TrapRunner());
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());
        declareAttackers(List.of(1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(trapRunner), null, attacker.getId());
        harness.passBothPriorities();

        assertThat(trapRunner.isTapped()).isTrue();
        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate before blockers are declared")
    void cannotActivateBeforeBlockersAreDeclared() {
        Permanent trapRunner = addCreatureReady(player1, new TrapRunner());
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        declareAttackers(List.of(1));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(trapRunner), null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("declare blockers");
    }

    @Test
    @DisplayName("Can activate during combat damage after blockers are declared")
    void canActivateDuringCombatDamageAfterBlockersAreDeclared() {
        Permanent trapRunner = addCreatureReady(player1, new TrapRunner());
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, battlefieldIndex(trapRunner), null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
    }

    @Test
    @DisplayName("Can target a creature that can't be blocked")
    void canTargetCreatureThatCantBeBlocked() {
        Permanent trapRunner = addCreatureReady(player1, new TrapRunner());
        Permanent blockadeRunner = addCreatureReady(player1, new BlockadeRunner());
        addCreatureReady(player1, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(blockadeRunner), null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(1, 2));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(trapRunner), null, blockadeRunner.getId());
        harness.passBothPriorities();

        assertThat(blockadeRunner.isBlockedWithoutBlockers()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an already blocked attacker")
    void cannotTargetBlockedAttacker() {
        Permanent trapRunner = addCreatureReady(player1, new TrapRunner());
        Permanent blockedAttacker = addCreatureReady(player1, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(trapRunner), null, blockedAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
