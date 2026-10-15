package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenalishInfantry.class, BenalishKnight.class})
class BenalishInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Benalish Infantry can form a band with one non-banding creature")
    void canFormBandWithNonBandingCreature() {
        Permanent infantry = addCreatureReady(player1, new BenalishInfantry());
        Permanent knight = addCreatureReady(player1, new BenalishKnight());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));

        assertThat(infantry.getBandId()).isNotNull();
        assertThat(infantry.getBandId()).isEqualTo(knight.getBandId());
    }

    @Test
    void canBandMultipleInfantryWithOneNonBandingCreature() {
        Permanent first = addCreatureReady(player1, new BenalishInfantry());
        Permanent second = addCreatureReady(player1, new BenalishInfantry());
        Permanent knight = addCreatureReady(player1, new BenalishKnight());

        declareBand(List.of(0, 1, 2));

        assertThat(first.getBandId()).isNotNull();
        assertThat(second.getBandId()).isEqualTo(first.getBandId());
        assertThat(knight.getBandId()).isEqualTo(first.getBandId());
    }

    @Test
    void cannotBandWithTwoNonBandingCreatures() {
        addCreatureReady(player1, new BenalishInfantry());
        addCreatureReady(player1, new BenalishKnight());
        addCreatureReady(player1, new BenalishKnight());

        assertThatThrownBy(() -> declareBand(List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature without banding");
    }

    @Test
    void blockingOneInfantryBlocksEveryMemberOfItsBand() {
        Permanent first = addCreatureReady(player1, new BenalishInfantry());
        Permanent second = addCreatureReady(player1, new BenalishInfantry());
        Permanent blocker = addCreatureReady(player2, new BenalishInfantry());

        declareBand(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getBlockingTargetIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void bandingBlockerLetsDefenderDivideAttackerDamage() {
        addCreatureReady(player1, new BenalishInfantry());
        Permanent first = addCreatureReady(player2, new BenalishInfantry());
        Permanent second = addCreatureReady(player2, new BenalishInfantry());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(1);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(second.getId(), 1));

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void bandingAttackerLetsAttackerDivideBlockerDamage() {
        Permanent first = addCreatureReady(player1, new BenalishInfantry());
        Permanent second = addCreatureReady(player1, new BenalishInfantry());
        addCreatureReady(player2, new BenalishInfantry());

        declareBand(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(1);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(second.getId(), 1));

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

    private void declareBand(List<Integer> indices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, indices, null, List.of(indices)));
    }
}