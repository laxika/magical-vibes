package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfCaltrops.class, GrizzlyBears.class})
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

        harness.passBothPriorities();

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

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isFalse();
    }

    @Test
    void doesNotGainBandingWhenANonWallAlsoBlocks() {
        addCreatureReady(player1, smallAttacker());
        Permanent wall = addCreatureReady(player2, new WallOfCaltrops());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isFalse();
        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        harness.handleCombatDamageAssigned(player1, 0, Map.of(wall.getId(), 1));
    }

    private GrizzlyBears smallAttacker() {
        GrizzlyBears attacker = new GrizzlyBears();
        attacker.setPower(1);
        attacker.setToughness(1);
        return attacker;
    }
}
