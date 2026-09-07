package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BenalishHero;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
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

@CardUsed({Pikemen.class, BenalishHero.class, GrayOgre.class})
class PikemenTest extends BaseCardTest {

    @Test
    void firstStrikeKillsBlockerBeforeItDealsCombatDamage() {
        Permanent pikemen = addCreatureReady(player1, new Pikemen());
        Permanent blocker = addCreatureReady(player2, new BenalishHero());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pikemen);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    void bandingAttackerLetsActivePlayerAssignBlockerDamage() {
        Permanent pikemen = addCreatureReady(player1, new Pikemen());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());
        Permanent blocker = addCreatureReady(player2, new GrayOgre());

        declareBand(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(pikemen.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pikemen);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonBander);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pikemen.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    void bandingBlockerLetsDefendingPlayerAssignAttackingDamage() {
        Permanent attacker = addCreatureReady(player1, new GrayOgre());
        Permanent pikemen = addCreatureReady(player2, new Pikemen());
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

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pikemen);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(plainBlocker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
    }

    private void declareBand(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, attackerIndices, null, List.of(attackerIndices)));
    }
}
