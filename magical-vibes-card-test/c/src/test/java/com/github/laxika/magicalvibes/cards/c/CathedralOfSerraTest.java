package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AdunOakenshield;
import com.github.laxika.magicalvibes.cards.e.ElvenRiders;
import com.github.laxika.magicalvibes.cards.j.Johan;
import com.github.laxika.magicalvibes.cards.t.TetsuoUmezawa;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
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

@CardUsed({CathedralOfSerra.class, AdunOakenshield.class, ElvenRiders.class, Johan.class, TetsuoUmezawa.class})
class CathedralOfSerraTest extends BaseCardTest {

    @Test
    @DisplayName("White legendary creatures can band with other legendary creatures")
    void whiteLegendaryCanBandWithOtherLegendary() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        Permanent whiteLegendary = addCreatureReady(player1, new Johan());
        Permanent otherLegendary = addCreatureReady(player1, new AdunOakenshield());

        declareBand(player1, List.of(1, 2));

        assertThat(whiteLegendary.getBandId()).isNotNull();
        assertThat(whiteLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    @Test
    @DisplayName("A band without a white legendary creature is rejected")
    void bandWithoutWhiteLegendaryCreatureIsRejected() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        addCreatureReady(player1, new AdunOakenshield());
        addCreatureReady(player1, new TetsuoUmezawa());

        beginAttackDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("The ability does not apply to white legendary creatures controlled by an opponent")
    void abilityDoesNotApplyToOpponentsCreatures() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        addCreatureReady(player2, new Johan());
        addCreatureReady(player2, new AdunOakenshield());

        beginAttackDeclaration(player2);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player2, List.of(0, 1), null, List.of(List.of(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    void whiteLegendaryCanFormASingleCreatureBand() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        Permanent legend = addCreatureReady(player1, new Johan());

        declareBand(player1, List.of(1));

        assertThat(legend.getBandId()).isNotNull();
    }

    @Test
    void nonlegendaryCreatureCannotJoinLegendaryBand() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        addCreatureReady(player1, new Johan());
        addCreatureReady(player1, new ElvenRiders());

        beginAttackDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackingBandControlsBlockerDamage() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        Permanent whiteLegend = addCreatureReady(player1, new Johan());
        Permanent otherLegend = addCreatureReady(player1, new AdunOakenshield());
        Permanent blocker = addCreatureReady(player2, new ElvenRiders());

        declareBand(player1, List.of(1, 2));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(blocker.getBlockingTargetIds()).contains(whiteLegend.getId(), otherLegend.getId());
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(3);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(otherLegend.getId(), 3));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(whiteLegend).doesNotContain(otherLegend);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void legendaryBlockingPairControlsAttackerDamage() {
        harness.addToBattlefield(player2, new CathedralOfSerra());
        Permanent attacker = addCreatureReady(player1, new TetsuoUmezawa());
        Permanent whiteLegend = addCreatureReady(player2, new Johan());
        Permanent otherLegend = addCreatureReady(player2, new AdunOakenshield());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0), new BlockerAssignment(2, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(3);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(whiteLegend.getId(), 3));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(whiteLegend, otherLegend);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    void additionalNonlegendaryBlockerDoesNotDisableLegendaryPairDamageAssignment() {
        harness.addToBattlefield(player2, new CathedralOfSerra());
        Permanent attacker = addCreatureReady(player1, new TetsuoUmezawa());
        Permanent whiteLegend = addCreatureReady(player2, new Johan());
        Permanent otherLegend = addCreatureReady(player2, new AdunOakenshield());
        Permanent nonlegend = addCreatureReady(player2, new ElvenRiders());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0), new BlockerAssignment(2, 0), new BlockerAssignment(3, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(3);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(nonlegend.getId(), 3));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(whiteLegend, otherLegend).doesNotContain(nonlegend);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    void existingBandSurvivesCathedralLeavingBattlefield() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        Permanent whiteLegend = addCreatureReady(player1, new Johan());
        Permanent otherLegend = addCreatureReady(player1, new AdunOakenshield());
        Permanent blocker = addCreatureReady(player2, new ElvenRiders());

        declareBand(player1, List.of(1, 2));
        gd.playerBattlefields.get(player1.getId()).removeFirst();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(whiteLegend.getBandId()).isNotNull().isEqualTo(otherLegend.getBandId());
        assertThat(blocker.getBlockingTargetIds()).contains(whiteLegend.getId(), otherLegend.getId());
    }

    private void beginAttackDeclaration(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void declareBand(Player player, List<Integer> attackers) {
        beginAttackDeclaration(player);
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player, attackers, null, List.of(attackers)));
    }
}
