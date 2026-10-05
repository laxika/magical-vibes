package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MesaPegasus;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KjeldoranSkycaptain.class, GrizzlyBears.class, MesaPegasus.class, KjeldoranRoyalGuard.class})
class KjeldoranSkycaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent skycaptain = addCreatureReady(player1, new KjeldoranSkycaptain());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skycaptain);
    }

    @Test
    @DisplayName("First strike prevents a smaller flying blocker from dealing combat damage")
    void firstStrikePreventsReciprocalDamage() {
        Permanent skycaptain = addCreatureReady(player1, new KjeldoranSkycaptain());
        Permanent blocker = addCreatureReady(player2, new MesaPegasus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skycaptain);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(skycaptain.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Banding lets the active player assign a blocker's combat damage")
    void bandingLetsActivePlayerAssignBlockerDamage() {
        Permanent skycaptain = addCreatureReady(player1, new KjeldoranSkycaptain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new KjeldoranRoyalGuard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(bears.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skycaptain);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("A banding blocker lets its controller divide attacker damage without a lethal assignment")
    void bandingBlockerLetsDefenderSplitDamage() {
        Permanent attacker = addCreatureReady(player1, new KjeldoranRoyalGuard());
        Permanent skycaptain = addCreatureReady(player2, new KjeldoranSkycaptain());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(skycaptain.getId(), 1, bears.getId(), 1));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(skycaptain, bears);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An attacking band cannot include two creatures without banding")
    void rejectsBandWithTwoNonBandingCreatures() {
        addCreatureReady(player1, new KjeldoranSkycaptain());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0, 1, 2),
                null, List.of(List.of(0, 1, 2))))
                .isInstanceOf(IllegalStateException.class);
    }
}
