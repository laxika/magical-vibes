package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RancidRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Skulk prevents a creature with greater power from blocking")
    void skulkPreventsGreaterPowerCreatureFromBlocking() {
        Permanent rats = addCreatureReady(player1, new RancidRats());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rats)));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(rats)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    @DisplayName("Deathtouch lets Rancid Rats destroy a tougher creature in combat")
    void deathtouchDestroysTougherBlocker() {
        Permanent rats = addCreatureReady(player1, new RancidRats());
        Permanent blocker = addCreatureReady(player2, new HornedTurtle());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rats)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(rats))));

        resolveCombat();

        harness.assertInGraveyard(player1, "Rancid Rats");
        harness.assertInGraveyard(player2, "Horned Turtle");
    }

    @Test
    @DisplayName("Skulk allows a creature with equal power to block")
    void skulkAllowsEqualPowerCreatureToBlock() {
        Permanent rats = addCreatureReady(player1, new RancidRats());
        Permanent blocker = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rats)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(rats))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
