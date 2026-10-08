package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.ShamblingStrider;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfShields.class, BalduvianBears.class, ShamblingStrider.class})
class WallOfShieldsTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents Wall of Shields from attacking")
    void cannotAttack() {
        addCreatureReady(player1, new WallOfShields());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Banding lets the defending player divide an attacker's combat damage")
    void bandingBlockerLetsDefenderDivideAttackerDamage() {
        Permanent attacker = addCreatureReady(player1, new ShamblingStrider());
        Permanent wall = addCreatureReady(player2, new WallOfShields());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(5);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(bears.getId(), 5));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Banding allows the defender to divide damage so both blockers survive")
    void defenderCanKeepBothBlockersAlive() {
        addCreatureReady(player1, new BalduvianBears());
        Permanent wall = addCreatureReady(player2, new WallOfShields());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player2, 0, Map.of(wall.getId(), 1, bears.getId(), 1));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall, bears);
        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.assertNotInGraveyard(player2, "Balduvian Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The attacking player cannot assign damage when a blocker has banding")
    void attackingPlayerCannotChooseDamageAssignment() {
        addCreatureReady(player1, new ShamblingStrider());
        Permanent wall = addCreatureReady(player2, new WallOfShields());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();

        assertThatThrownBy(() -> gs.handleCombatDamageAssigned(gd, player1, 0, Map.of(wall.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);
        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());

        harness.handleCombatDamageAssigned(player2, 0, Map.of(bears.getId(), 5));

        harness.assertOnBattlefield(player2, "Wall of Shields");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }
}
