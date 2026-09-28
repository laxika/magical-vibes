package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FortifiedArea.class, WallOfWood.class, GrizzlyBears.class})
class FortifiedAreaTest extends BaseCardTest {

    @Test
    @DisplayName("Wall creatures you control get +1/+0")
    void buffsOwnWalls() {
        harness.addToBattlefield(player1, new FortifiedArea());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfWood());

        // Wall of Wood is 0/3 → 1/3
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }

    @Test
    @DisplayName("Wall creatures you control gain banding")
    void wallsGainBanding() {
        harness.addToBattlefield(player1, new FortifiedArea());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfWood());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isTrue();
        // Non-Wall creatures you control are unaffected.
        assertThat(gqs.hasKeyword(gd, bears, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Granted banding lets the defending player assign an attacking creature's damage")
    void grantedBandingLetsDefenderAssignAttackerDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FortifiedArea());
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(bears.getId(), 2));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not buff non-Wall creatures")
    void doesNotBuffNonWalls() {
        harness.addToBattlefield(player1, new FortifiedArea());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Wall creatures")
    void doesNotBuffOpponentWalls() {
        harness.addToBattlefield(player1, new FortifiedArea());
        Permanent opponentWall = harness.addToBattlefieldAndReturn(player2, new WallOfWood());

        assertThat(gqs.getEffectivePower(gd, opponentWall)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentWall)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not grant banding to opponent's Wall creatures")
    void doesNotGrantBandingToOpponentWalls() {
        harness.addToBattlefield(player1, new FortifiedArea());
        Permanent opponentWall = harness.addToBattlefieldAndReturn(player2, new WallOfWood());

        assertThat(gqs.hasKeyword(gd, opponentWall, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Bonus is removed when Fortified Area leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent fortifiedArea = harness.addToBattlefieldAndReturn(player1, new FortifiedArea());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfWood());
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(fortifiedArea);

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }

    @Test
    @DisplayName("Banding is removed when Fortified Area leaves the battlefield")
    void bandingRemovedWhenSourceLeaves() {
        Permanent fortifiedArea = harness.addToBattlefieldAndReturn(player1, new FortifiedArea());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfWood());
        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fortifiedArea);

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isFalse();
    }
}
