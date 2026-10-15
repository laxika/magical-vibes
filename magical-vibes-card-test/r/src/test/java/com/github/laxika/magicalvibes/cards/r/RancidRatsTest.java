package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MoldgrafScavenger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RancidRats.class, HillGiant.class, HornedTurtle.class, LlanowarElves.class,
        MoldgrafScavenger.class})
class RancidRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Skulk prevents a creature with greater power from blocking")
    void skulkPreventsGreaterPowerCreatureFromBlocking() {
        Permanent rats = addCreatureReady(player1, new RancidRats());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rats)));

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

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rats)));
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

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rats)));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(rats))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A lower-power blocker is legal and dies to deathtouch without killing the Rats")
    void lowerPowerBlockerDiesWhileRatsSurvive() {
        Permanent rats = addCreatureReady(player1, new RancidRats());
        Permanent blocker = addCreatureReady(player2, new MoldgrafScavenger());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rats)));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(rats))));

        resolveCombat();

        harness.assertOnBattlefield(player1, "Rancid Rats");
        harness.assertInGraveyard(player2, "Moldgraf Scavenger");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Rancid Rats can block a greater-power attacker and destroy it with deathtouch")
    void deathtouchDestroysGreaterPowerAttackerWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent rats = addCreatureReady(player2, new RancidRats());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(rats),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        resolveCombat();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Rancid Rats");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
