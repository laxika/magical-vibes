package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarElephant.class, GrizzlyBears.class, RagingGoblin.class})
class WarElephantTest extends BaseCardTest {

    @Test
    @DisplayName("War Elephant can band with one non-banding attacker")
    void canBandWithNonBandingAttacker() {
        Permanent elephant = addCreatureReady(player1, new WarElephant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareBand();

        assertThat(elephant.getBandId()).isNotNull();
        assertThat(elephant.getBandId()).isEqualTo(bears.getBandId());
    }

    @Test
    @DisplayName("War Elephant tramples excess combat damage over a blocker")
    void tramplesExcessDamage() {
        harness.setLife(player2, 20);
        Permanent elephant = addCreatureReady(player1, new WarElephant());
        elephant.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RagingGoblin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Raging Goblin");
        harness.assertOnBattlefield(player1, "War Elephant");
    }

    @Test
    void blockingOneMemberBlocksBothBandMembers() {
        Permanent elephant = addCreatureReady(player1, new WarElephant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareBand();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargetIds()).containsExactlyInAnyOrder(elephant.getId(), bears.getId());
    }

    @Test
    void bandingBlockerLetsDefenderChooseAttackerDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent elephant = addCreatureReady(player2, new WarElephant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        harness.handleCombatDamageAssigned(player2, 0, Map.of(bears.getId(), 2));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elephant).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void bandingAttackerLetsAttackerChooseBlockerDamage() {
        Permanent elephant = addCreatureReady(player1, new WarElephant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareBand();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        harness.handleCombatDamageAssigned(player1, 0, Map.of(bears.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elephant).doesNotContain(bears);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    private void declareBand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));
    }
}
