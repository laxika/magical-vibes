package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdunOakenshield;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.j.Johan;
import com.github.laxika.magicalvibes.cards.p.PrincessLucrezia;
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

@CardUsed({SeafarersQuay.class, PrincessLucrezia.class, Johan.class,
        AdunOakenshield.class, DurkwoodBoars.class})
class SeafarersQuayTest extends BaseCardTest {

    @Test
    @DisplayName("Blue legendary creatures can band with other legendary creatures")
    void blueLegendaryCanBandWithOtherLegendary() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        Permanent blueLegendary = addCreatureReady(player1, new PrincessLucrezia());
        Permanent otherLegendary = addCreatureReady(player1, new Johan());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));

        assertThat(blueLegendary.getBandId()).isNotNull();
        assertThat(blueLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    @Test
    @DisplayName("A band with a nonlegendary creature is rejected")
    void bandWithNonlegendaryCreatureIsRejected() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        addCreatureReady(player1, new PrincessLucrezia());
        addCreatureReady(player1, new DurkwoodBoars());

        beginAttackerDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("A non-blue legendary creature does not receive the bands-with-other ability")
    void nonBlueLegendaryCannotFormBand() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        addCreatureReady(player1, new Johan());
        addCreatureReady(player1, new AdunOakenshield());

        beginAttackerDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("The ability does not apply to legendary creatures controlled by an opponent")
    void abilityDoesNotApplyToOpponentsCreatures() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        addCreatureReady(player2, new PrincessLucrezia());
        addCreatureReady(player2, new Johan());

        beginAttackerDeclaration(player2);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player2, List.of(0, 1), null, List.of(List.of(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("A blue legendary creature can form a band by itself")
    void blueLegendaryCanFormSingleCreatureBand() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        Permanent legend = addCreatureReady(player1, new PrincessLucrezia());

        declareBand(player1, List.of(1), List.of(List.of(1)));

        assertThat(legend.getBandId()).isNotNull();
    }

    @Test
    @DisplayName("A band can contain any number of other legendary creatures")
    void bandCanContainMultipleOtherLegendaries() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        Permanent blueLegend = addCreatureReady(player1, new PrincessLucrezia());
        Permanent first = addCreatureReady(player1, new Johan());
        Permanent second = addCreatureReady(player1, new AdunOakenshield());

        declareBand(player1, List.of(1, 2, 3), List.of(List.of(1, 2, 3)));

        assertThat(blueLegend.getBandId()).isNotNull();
        assertThat(first.getBandId()).isEqualTo(blueLegend.getBandId());
        assertThat(second.getBandId()).isEqualTo(blueLegend.getBandId());
    }

    @Test
    @DisplayName("An enabled attacking band controls the blocker's damage assignment")
    void attackingBandControlsBlockerDamage() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        Permanent blueLegend = addCreatureReady(player1, new PrincessLucrezia());
        Permanent otherLegend = addCreatureReady(player1, new Johan());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(blocker.getBlockingTargetIds()).contains(blueLegend.getId(), otherLegend.getId());
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(4);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blueLegend.getId(), 4));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherLegend).doesNotContain(blueLegend);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("A legendary blocking pair controls attacker damage even with a nonlegendary blocker")
    void legendaryBlockingPairControlsAttackerDamage() {
        harness.addToBattlefield(player2, new SeafarersQuay());
        Permanent attacker = addCreatureReady(player1, new Johan());
        Permanent blueLegend = addCreatureReady(player2, new PrincessLucrezia());
        Permanent otherLegend = addCreatureReady(player2, new AdunOakenshield());
        Permanent nonlegend = addCreatureReady(player2, new DurkwoodBoars());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0), new BlockerAssignment(2, 0),
                new BlockerAssignment(3, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(5);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(nonlegend.getId(), 5));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blueLegend, otherLegend).doesNotContain(nonlegend);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("Removing the Quay before combat removes the ability to form a band")
    void removingQuayRemovesAbility() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        Permanent quay = gd.playerBattlefields.get(player1.getId()).getFirst();
        addCreatureReady(player1, new PrincessLucrezia());
        addCreatureReady(player1, new Johan());
        gd.playerBattlefields.get(player1.getId()).remove(quay);

        beginAttackerDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }
    private void beginAttackerDeclaration(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void declareBand(Player player, List<Integer> attackers, List<List<Integer>> bands) {
        beginAttackerDeclaration(player);
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player, attackers, null, bands));
    }
}
