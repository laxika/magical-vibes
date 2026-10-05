package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
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

@CardUsed({KjeldoranEscort.class, ElvishRanger.class})
class KjeldoranEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Banding makes a blocker block the whole attacking band")
    void bandingSharesBlockersAcrossTheBand() {
        Permanent escort = addCreatureReady(player1, new KjeldoranEscort());
        Permanent ranger = addCreatureReady(player1, new ElvishRanger());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargetIds()).contains(escort.getId(), ranger.getId());
    }

    @Test
    @DisplayName("The attacking player divides a blocker's damage when it blocks the band")
    void attackerDividesBlockerDamage() {
        Permanent escort = addCreatureReady(player1, new KjeldoranEscort());
        Permanent ranger = addCreatureReady(player1, new ElvishRanger());
        harness.addToBattlefield(player2, new ElvishRanger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt = gd.interaction
                .activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(4);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(ranger.getId(), 4));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(escort).doesNotContain(ranger);
        harness.assertInGraveyard(player1, "Elvish Ranger");
        harness.assertInGraveyard(player2, "Elvish Ranger");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A banding blocker lets the defending player divide attacking damage without a band")
    void defenderDividesAttackerDamage() {
        addCreatureReady(player1, new ElvishRanger());
        Permanent escort = harness.addToBattlefieldAndReturn(player2, new KjeldoranEscort());
        Permanent ranger = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt = gd.interaction
                .activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(4);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(ranger.getId(), 4));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(escort).doesNotContain(ranger);
        harness.assertInGraveyard(player1, "Elvish Ranger");
        harness.assertInGraveyard(player2, "Elvish Ranger");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An ordinary attacking band cannot include two creatures without banding")
    void rejectsTwoNonbandingMembers() {
        addCreatureReady(player1, new KjeldoranEscort());
        addCreatureReady(player1, new ElvishRanger());
        addCreatureReady(player1, new ElvishRanger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0, 1, 2),
                null, List.of(List.of(0, 1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature without banding");
    }
}
