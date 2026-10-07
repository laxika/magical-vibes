package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
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

@CardUsed({TimberWolves.class, GrayOgre.class})
class TimberWolvesTest extends BaseCardTest {

    @Test
    void canBandWithOneNonBandingAttacker() {
        Permanent wolves = addCreatureReady(player1, new TimberWolves());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());

        declareBand(List.of(0, 1), List.of(List.of(0, 1)));

        assertThat(wolves.getBandId()).isNotNull();
        assertThat(wolves.getBandId()).isEqualTo(nonBander.getBandId());
    }

    @Test
    void canDeclareBandWithOnlyBandingCreature() {
        Permanent wolves = addCreatureReady(player1, new TimberWolves());

        declareBand(List.of(0), List.of(List.of(0)));

        assertThat(wolves.getBandId()).isNotNull();
    }

    @Test
    void canBandMultipleBandingAttackersWithOneNonBandingAttacker() {
        Permanent wolves = addCreatureReady(player1, new TimberWolves());
        Permanent secondWolves = addCreatureReady(player1, new TimberWolves());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());

        declareBand(List.of(0, 1, 2), List.of(List.of(0, 1, 2)));

        assertThat(wolves.getBandId()).isNotNull();
        assertThat(secondWolves.getBandId()).isEqualTo(wolves.getBandId());
        assertThat(nonBander.getBandId()).isEqualTo(wolves.getBandId());
    }

    @Test
    void blockingOneBandMemberBlocksTheEntireBand() {
        Permanent wolves = addCreatureReady(player1, new TimberWolves());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());
        Permanent blocker = addCreatureReady(player2, new GrayOgre());

        declareBand(List.of(0, 1), List.of(List.of(0, 1)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargetIds()).contains(wolves.getId(), nonBander.getId());
    }

    @Test
    void bandingBlockerLetsDefendingPlayerAssignAttackingDamage() {
        Permanent attacker = addCreatureReady(player1, new GrayOgre());
        Permanent bandingBlocker = addCreatureReady(player2, new TimberWolves());
        Permanent plainBlocker = addCreatureReady(player2, new GrayOgre());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(plainBlocker.getId(), 2));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bandingBlocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(plainBlocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    void bandingAttackerLetsItsControllerAssignBlockerDamage() {
        Permanent wolves = addCreatureReady(player1, new TimberWolves());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());
        Permanent blocker = addCreatureReady(player2, new GrayOgre());

        declareBand(List.of(0, 1), List.of(List.of(0, 1)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(nonBander.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wolves);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nonBander);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void cannotBandWithTwoNonBandingAttackers() {
        addCreatureReady(player1, new TimberWolves());
        addCreatureReady(player1, new GrayOgre());
        addCreatureReady(player1, new GrayOgre());

        assertThatThrownBy(() -> declareBand(List.of(0, 1, 2), List.of(List.of(0, 1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature without banding");
    }

    @Test
    void cannotPutTheSameCreatureInTwoBands() {
        addCreatureReady(player1, new TimberWolves());
        addCreatureReady(player1, new TimberWolves());
        addCreatureReady(player1, new GrayOgre());

        assertThatThrownBy(() -> declareBand(List.of(0, 1, 2),
                List.of(List.of(0, 2), List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than one band");
    }

    @Test
    void bandingDoesNotRequireAttackingInABand() {
        Permanent wolves = addCreatureReady(player1, new TimberWolves());
        addCreatureReady(player2, new GrayOgre());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(wolves.isAttacking()).isTrue();
        assertThat(wolves.getBandId()).isNull();
    }

    private void declareBand(List<Integer> attackerIndices, List<List<Integer>> bands) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, attackerIndices, null, bands));
    }
}
