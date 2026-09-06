package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
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

@CardUsed({BenalishHero.class, GrayOgre.class})
class BenalishHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Can band with one non-banding attacker")
    void canBandWithOneNonBandingAttacker() {
        Permanent hero = addCreatureReady(player1, new BenalishHero());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());

        declareBand(List.of(0, 1), List.of(List.of(0, 1)));

        assertThat(hero.getBandId()).isNotNull();
        assertThat(hero.getBandId()).isEqualTo(nonBander.getBandId());
    }

    @Test
    @DisplayName("Can declare a band containing only a banding creature")
    void canDeclareBandWithOnlyBandingCreature() {
        Permanent hero = addCreatureReady(player1, new BenalishHero());

        declareBand(List.of(0), List.of(List.of(0)));

        assertThat(hero.getBandId()).isNotNull();
    }

    @Test
    @DisplayName("Can band multiple banding attackers with one non-banding attacker")
    void canBandMultipleBandingAttackersWithOneNonBandingAttacker() {
        Permanent hero = addCreatureReady(player1, new BenalishHero());
        Permanent secondHero = addCreatureReady(player1, new BenalishHero());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());

        declareBand(List.of(0, 1, 2), List.of(List.of(0, 1, 2)));

        assertThat(hero.getBandId()).isNotNull();
        assertThat(secondHero.getBandId()).isEqualTo(hero.getBandId());
        assertThat(nonBander.getBandId()).isEqualTo(hero.getBandId());
    }

    @Test
    void blockingOneBandMemberBlocksTheEntireBand() {
        Permanent hero = addCreatureReady(player1, new BenalishHero());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());
        Permanent blocker = addCreatureReady(player2, new GrayOgre());

        declareBand(List.of(0, 1), List.of(List.of(0, 1)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargetIds()).contains(hero.getId(), nonBander.getId());
    }

    @Test
    void bandingBlockerLetsDefendingPlayerAssignAttackingDamage() {
        Permanent attacker = addCreatureReady(player1, new GrayOgre());
        Permanent bandingBlocker = addCreatureReady(player2, new BenalishHero());
        Permanent plainBlocker = addCreatureReady(player2, new GrayOgre());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
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
        Permanent hero = addCreatureReady(player1, new BenalishHero());
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

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hero);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nonBander);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
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
