package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeremkoGriffin.class, FemerefScouts.class})
class TeremkoGriffinTest extends BaseCardTest {

    @Test
    void groundCreatureCannotBlockGriffinAttackingAlone() {
        addCreatureReady(player1, new TeremkoGriffin());
        addCreatureReady(player2, new FemerefScouts());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    void flyingCreatureCanBlockGriffin() {
        addCreatureReady(player1, new TeremkoGriffin());
        Permanent blocker = addCreatureReady(player2, new TeremkoGriffin());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.getBlockingTargetIds()).containsExactly(
                gd.playerBattlefields.get(player1.getId()).getFirst().getId());
    }

    @Test
    void blockingGroundBandMemberAlsoBlocksFlyingGriffin() {
        Permanent griffin = addCreatureReady(player1, new TeremkoGriffin());
        Permanent scout = addCreatureReady(player1, new FemerefScouts());
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());
        prepareBandAttack();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> gs.declareAttackers(
                gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 1))));

        assertThat(blocker.getBlockingTargetIds()).containsExactlyInAnyOrder(griffin.getId(), scout.getId());
    }

    @Test
    void bandCannotContainTwoCreaturesWithoutBanding() {
        addCreatureReady(player1, new TeremkoGriffin());
        addCreatureReady(player1, new FemerefScouts());
        addCreatureReady(player1, new FemerefScouts());
        prepareBandAttack();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0, 1, 2),
                null, List.of(List.of(0, 1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without banding");
    }

    @Test
    void defendingControllerAssignsDamageWhenGriffinBlocks() {
        Permanent attacker = addCreatureReady(player1, new FemerefScouts());
        Permanent griffin = addCreatureReady(player2, new TeremkoGriffin());
        Permanent scout = addCreatureReady(player2, new FemerefScouts());
        attacker.setAttacking(true);
        griffin.setBlocking(true);
        griffin.addBlockingTarget(0);
        scout.setBlocking(true);
        scout.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        var prompt = gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () ->
                harness.handleCombatDamageAssigned(player2, 0, Map.of(scout.getId(), 1)));

        assertThat(griffin.getMarkedDamage()).isZero();
        assertThat(scout.getMarkedDamage()).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void attackingControllerAssignsBlockerDamageAcrossBand() {
        Permanent griffin = addCreatureReady(player1, new TeremkoGriffin());
        Permanent scout = addCreatureReady(player1, new FemerefScouts());
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());
        prepareBandAttack();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> gs.declareAttackers(
                gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 1))));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        var prompt = gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () ->
                harness.handleCombatDamageAssigned(player1, 0, Map.of(scout.getId(), 1)));

        assertThat(griffin.getMarkedDamage()).isZero();
        assertThat(scout.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    private void prepareBandAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}

