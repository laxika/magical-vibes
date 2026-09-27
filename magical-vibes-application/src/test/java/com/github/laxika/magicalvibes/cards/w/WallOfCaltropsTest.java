package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WallOfCaltrops.class, WallOfEarth.class, BarbaryApes.class})
class WallOfCaltropsTest extends BaseCardTest {

    @Test
    void gainsBandingWhenTwoWallsBlockTheSameCreature() {
        addCreatureReady(player1, smallAttacker());
        Permanent firstWall = addCreatureReady(player2, new WallOfCaltrops());
        Permanent secondWall = addCreatureReady(player2, new WallOfCaltrops());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        resolveAllTriggers();
        resolveCombat();

        assertThat(gqs.hasKeyword(gd, firstWall, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondWall, Keyword.BANDING)).isTrue();
        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        harness.handleCombatDamageAssigned(player2, 0, Map.of(secondWall.getId(), 1));
    }

    @Test
    void doesNotGainBandingWithOnlyOneWallBlocking() {
        addCreatureReady(player1, smallAttacker());
        Permanent wall = addCreatureReady(player2, new WallOfCaltrops());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isFalse();
    }

    @Test
    void doesNotGainBandingWhenANonWallAlsoBlocks() {
        addCreatureReady(player1, smallAttacker());
        Permanent wall = addCreatureReady(player2, new WallOfCaltrops());
        addCreatureReady(player2, new BarbaryApes());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        resolveCombat();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isFalse();
        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        harness.handleCombatDamageAssigned(player1, 0, Map.of(wall.getId(), 1));
    }

    @Test
    void gainsBandingWithAnotherWallCreature() {
        addCreatureReady(player1, smallAttacker());
        Permanent wallOfCaltrops = addCreatureReady(player2, new WallOfCaltrops());
        Permanent otherWall = addCreatureReady(player2, new WallOfEarth());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        resolveAllTriggers();
        resolveCombat(player1);

        assertThat(gqs.hasKeyword(gd, wallOfCaltrops, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherWall, Keyword.BANDING)).isFalse();
        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        harness.handleCombatDamageAssigned(player2, 0, Map.of(otherWall.getId(), 1));
    }

    @Test
    void doesNotCountWallsBlockingDifferentAttackers() {
        addCreatureReady(player1, smallAttacker());
        addCreatureReady(player1, smallAttacker());
        Permanent firstWall = addCreatureReady(player2, new WallOfCaltrops());
        Permanent secondWall = addCreatureReady(player2, new WallOfCaltrops());

        declareAttackers(player1, List.of(0, 1));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        resolveCombat(player1);

        assertThat(gqs.hasKeyword(gd, firstWall, Keyword.BANDING)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondWall, Keyword.BANDING)).isFalse();
    }

    @Test
    void bandingExpiresAtEndOfTurn() {
        addCreatureReady(player1, smallAttacker());
        Permanent wallOfCaltrops = addCreatureReady(player2, new WallOfCaltrops());
        Permanent otherWall = addCreatureReady(player2, new WallOfEarth());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        resolveAllTriggers();
        resolveCombat();

        assertThat(gqs.hasKeyword(gd, wallOfCaltrops, Keyword.BANDING)).isTrue();
        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        harness.handleCombatDamageAssigned(player2, 0, Map.of(otherWall.getId(), 1));
        assertThat(gqs.hasKeyword(gd, wallOfCaltrops, Keyword.BANDING)).isTrue();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, wallOfCaltrops, Keyword.BANDING)).isFalse();
    }

    private BarbaryApes smallAttacker() {
        BarbaryApes attacker = new BarbaryApes();
        attacker.setPower(1);
        attacker.setToughness(1);
        return attacker;
    }
}
