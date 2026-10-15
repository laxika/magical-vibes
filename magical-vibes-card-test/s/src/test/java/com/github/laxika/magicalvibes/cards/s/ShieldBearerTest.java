package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
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

@CardUsed({ShieldBearer.class, BalduvianBears.class})
class ShieldBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Banding attacker lets the active player assign blocker damage")
    void bandingAttackerLetsActivePlayerAssignBlockerDamage() {
        Permanent shieldBearer = addCreatureReady(player1, new ShieldBearer());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

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
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(shieldBearer.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shieldBearer, bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Banding blocker lets the defending player assign attacker damage")
    void bandingBlockerLetsDefendingPlayerAssignAttackerDamage() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent shieldBearer = addCreatureReady(player2, new ShieldBearer());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt = gd.interaction
                .activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(blocker.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shieldBearer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("An attacking band cannot contain two creatures without banding")
    void rejectsTwoNonbandingCreaturesInOneBand() {
        addCreatureReady(player1, new ShieldBearer());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(0, 1, 2), null, List.of(List.of(0, 1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature without banding");
    }

    @Test
    @DisplayName("Multiple banding creatures can attack with one nonbanding creature")
    void allowsMultipleBandingCreaturesWithOneNonbandingCreature() {
        Permanent firstBearer = addCreatureReady(player1, new ShieldBearer());
        Permanent secondBearer = addCreatureReady(player1, new ShieldBearer());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1, 2), null, List.of(List.of(0, 1, 2)));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 2)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt = gd.interaction
                .activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(firstBearer.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstBearer, secondBearer, bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Banding does not automatically group separately declared attackers")
    void separatelyDeclaredAttackersDoNotShareBlocks() {
        Permanent shieldBearer = addCreatureReady(player1, new ShieldBearer());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shieldBearer, bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        harness.assertLife(player2, 18);
    }
}
