package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.r.RootbreakerWurm;
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

@CardUsed({SiegeBehemoth.class, GrizzlyBears.class, RagingGoblin.class, RootbreakerWurm.class})
class SiegeBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("A blocked Siege Behemoth may assign combat damage to the defending player")
    void blockedSiegeBehemothMayAssignDamageAsThoughUnblocked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SiegeBehemoth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Siege Behemoth also grants the option to another attacking creature")
    void grantsOptionToAnotherCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SiegeBehemoth());
        addCreatureReady(player1, new RagingGoblin());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 1, Map.of(player2.getId(), 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the option assigns damage to the blocker normally")
    void decliningUsesNormalBlockedAssignment() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SiegeBehemoth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("The ability does not apply while Siege Behemoth is not attacking")
    void doesNotApplyWhileNotAttacking() {
        harness.setLife(player2, 20);
        Permanent behemoth = addCreatureReady(player1, new SiegeBehemoth());
        addCreatureReady(player1, new RagingGoblin());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(behemoth);
    }

    @Test
    @DisplayName("A creature with trample may bypass blockers without assigning them lethal damage")
    void tramplingCreatureMayAssignAllDamageAsThoughUnblocked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SiegeBehemoth());
        Permanent attacker = addCreatureReady(player1, new RootbreakerWurm());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 1, Map.of(player2.getId(), 6));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The controller still chooses whether to bypass blocking when no blockers remain")
    void blockedCreatureWithoutRemainingBlockersStillOffersChoice() {
        Permanent behemoth = addCreatureReady(player1, new SiegeBehemoth());
        behemoth.setAttacking(true);
        behemoth.setAttackTarget(player2.getId());
        behemoth.setBlockedWithoutBlockers(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
    }

    @Test
    @DisplayName("Each attacking creature makes an independent choice to bypass blockers")
    void canBypassForOneCreatureAndDeclineForAnother() {
        harness.setLife(player2, 20);
        Permanent behemoth = addCreatureReady(player1, new SiegeBehemoth());
        addCreatureReady(player1, new RagingGoblin());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));
        harness.handleCombatDamageAssigned(player1, 1, Map.of(secondBlocker.getId(), 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(1);
        assertThat(behemoth.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Siege Behemoth does not grant the bypass option")
    void doesNotGrantOptionToOpponentsCreatures() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RagingGoblin());
        Permanent behemoth = addCreatureReady(player2, new SiegeBehemoth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(behemoth.getMarkedDamage()).isEqualTo(1);
    }
}
