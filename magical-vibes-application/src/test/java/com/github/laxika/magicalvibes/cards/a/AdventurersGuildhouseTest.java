package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvenRiders;
import com.github.laxika.magicalvibes.cards.j.Johan;
import com.github.laxika.magicalvibes.cards.l.LadyEvangela;
import com.github.laxika.magicalvibes.cards.t.TetsuoUmezawa;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({AdventurersGuildhouse.class, AdunOakenshield.class, ElvenRiders.class, Johan.class, LadyEvangela.class, TetsuoUmezawa.class})
class AdventurersGuildhouseTest extends BaseCardTest {

    @Test
    @DisplayName("Green legendary creatures can band with other legendary creatures")
    void greenLegendaryCreaturesCanBandWithOtherLegendaries() {
        addGuildhouse(player1);
        Permanent greenLegendary = addCreatureReady(player1, new AdunOakenshield());
        Permanent otherLegendary = addCreatureReady(player1, new Johan());

        declareBand(player1, List.of(1, 2));

        assertThat(greenLegendary.getBandId()).isNotNull();
        assertThat(greenLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    @Test
    @DisplayName("A band with a nonlegendary creature is rejected")
    void bandWithNonlegendaryCreatureIsRejected() {
        addGuildhouse(player1);
        addCreatureReady(player1, new AdunOakenshield());
        addCreatureReady(player1, new ElvenRiders());

        beginAttackDeclaration(player1);

        assertThatThrownBy(() -> harness.getGameService().declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("Non-green legendary creatures do not receive the bands-with-other ability")
    void nonGreenLegendariesCannotFormABand() {
        addGuildhouse(player1);
        addCreatureReady(player1, new LadyEvangela());
        addCreatureReady(player1, new TetsuoUmezawa());

        beginAttackDeclaration(player1);

        assertThatThrownBy(() -> harness.getGameService().declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("The ability applies only to legendary creatures controlled by the Guildhouse's controller")
    void abilityDoesNotApplyToAnOpponentsCreatures() {
        addGuildhouse(player1);
        addCreatureReady(player2, new AdunOakenshield());
        addCreatureReady(player2, new TetsuoUmezawa());

        beginAttackDeclaration(player2);

        assertThatThrownBy(() -> harness.getGameService().declareAttackers(
                gd, player2, List.of(0, 1), null, List.of(List.of(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("A Guildhouse-enabled band lets its controller assign a blocker's combat damage")
    void enabledBandLetsAttackingControllerAssignBlockerDamage() {
        addGuildhouse(player1);
        Permanent greenLegend = addCreatureReady(player1, new AdunOakenshield());
        Permanent otherLegend = addCreatureReady(player1, new TetsuoUmezawa());
        Permanent blocker = addCreatureReady(player2, new ElvenRiders());

        declareBand(player1, List.of(1, 2));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(greenLegend.getId(), otherLegend.getId());
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(3);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(greenLegend.getId(), 3));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(greenLegend);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherLegend);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    private void addGuildhouse(Player player) {
        harness.addToBattlefield(player, new AdventurersGuildhouse());
    }

    private void beginAttackDeclaration(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void declareBand(Player activePlayer, List<Integer> attackerIndices) {
        beginAttackDeclaration(activePlayer);
        harness.inMutationScope(() -> harness.getCombatAttackService().declareAttackers(
                gd,
                activePlayer,
                attackerIndices,
                null,
                List.of(attackerIndices)));
    }
}
