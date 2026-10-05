package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PearledUnicorn;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MesaPegasus.class, PearledUnicorn.class})
class MesaPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Mesa Pegasus")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent pegasus = addCreatureReady(player1, new MesaPegasus());
        addCreatureReady(player2, new PearledUnicorn());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pegasus.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Mesa Pegasus can form a band with one other creature")
    void canFormBandWithOneOtherCreature() {
        Permanent pegasus = addCreatureReady(player1, new MesaPegasus());
        Permanent unicorn = addCreatureReady(player1, new PearledUnicorn());

        beginAttackerDeclaration();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));

        assertThat(pegasus.getBandId()).isNotNull();
        assertThat(unicorn.getBandId()).isEqualTo(pegasus.getBandId());
    }

    @Test
    @DisplayName("Mesa Pegasus banding makes a ground blocker block its band-mate")
    void bandingSharesBlocksWithBandMate() {
        Permanent pegasus = addCreatureReady(player1, new MesaPegasus());
        addCreatureReady(player1, new PearledUnicorn());
        Permanent blocker = addCreatureReady(player2, new PearledUnicorn());

        beginAttackerDeclaration();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargetIds()).contains(pegasus.getId());
    }

    @Test
    @DisplayName("Mesa Pegasus may be declared as a one-creature band")
    void mayBeDeclaredAsOneCreatureBand() {
        Permanent pegasus = addCreatureReady(player1, new MesaPegasus());

        beginAttackerDeclaration();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0), null, List.of(List.of(0))));

        assertThat(pegasus.getBandId()).isNotNull();
    }

    @Test
    void bandingBlockerLetsDefenderAssignAttackerDamage() {
        Permanent attacker = addCreatureReady(player1, new PearledUnicorn());
        Permanent pegasus = addCreatureReady(player2, new MesaPegasus());
        Permanent unicorn = addCreatureReady(player2, new PearledUnicorn());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(unicorn.getId(), 2));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pegasus).doesNotContain(unicorn);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    void bandingAttackerLetsAttackerAssignBlockerDamage() {
        Permanent pegasus = addCreatureReady(player1, new MesaPegasus());
        Permanent unicorn = addCreatureReady(player1, new PearledUnicorn());
        Permanent blocker = addCreatureReady(player2, new PearledUnicorn());

        beginAttackerDeclaration();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(unicorn.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pegasus).doesNotContain(unicorn);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void canBlockAnotherFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new MesaPegasus());
        Permanent blocker = addCreatureReady(player2, new MesaPegasus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void cannotBandWithTwoCreaturesWithoutBanding() {
        addCreatureReady(player1, new MesaPegasus());
        addCreatureReady(player1, new PearledUnicorn());
        addCreatureReady(player1, new PearledUnicorn());

        beginAttackerDeclaration();
        assertThatThrownBy(() -> harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1, 2), null, List.of(List.of(0, 1, 2)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature without banding");
    }

    private void beginAttackerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
