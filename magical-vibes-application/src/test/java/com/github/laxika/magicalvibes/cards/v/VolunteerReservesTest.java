package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({VolunteerReserves.class, BenalishKnight.class})
class VolunteerReservesTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Volunteer Reserves")
    void paysCumulativeUpkeep() {
        Permanent reserves = harness.addToBattlefieldAndReturn(player1, new VolunteerReserves());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(reserves.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(reserves);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Volunteer Reserves")
    void declineSacrifices() {
        Permanent reserves = harness.addToBattlefieldAndReturn(player1, new VolunteerReserves());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(reserves);
        harness.assertInGraveyard(player1, "Volunteer Reserves");
    }

    @Test
    @DisplayName("Cumulative upkeep requires one mana for each age counter")
    void cumulativeUpkeepEscalates() {
        Permanent reserves = harness.addToBattlefieldAndReturn(player1, new VolunteerReserves());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(reserves.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(reserves);
        harness.assertInGraveyard(player1, "Volunteer Reserves");
    }

    @Test
    @DisplayName("Volunteer Reserves can form a band with one non-banding creature")
    void canFormBandWithNonBandingCreature() {
        Permanent reserves = addCreatureReady(player1, new VolunteerReserves());
        Permanent knight = addCreatureReady(player1, new BenalishKnight());

        declareBand(List.of(0, 1), List.of(List.of(0, 1)));

        assertThat(reserves.getBandId()).isNotNull();
        assertThat(reserves.getBandId()).isEqualTo(knight.getBandId());
    }

    @Test
    void payingFullSecondUpkeepKeepsReserves() {
        Permanent reserves = harness.addToBattlefieldAndReturn(player1, new VolunteerReserves());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(reserves.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(reserves);
        harness.assertNotInGraveyard(player1, "Volunteer Reserves");
    }

    @Test
    void opponentUpkeepDoesNotRequirePayment() {
        Permanent reserves = harness.addToBattlefieldAndReturn(player1, new VolunteerReserves());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(reserves.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(reserves);
    }

    @Test
    void cannotBandWithTwoNonBandingCreatures() {
        addCreatureReady(player1, new VolunteerReserves());
        addCreatureReady(player1, new BenalishKnight());
        addCreatureReady(player1, new BenalishKnight());

        assertThatThrownBy(() -> declareBand(List.of(0, 1, 2), List.of(List.of(0, 1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature without banding");
    }

    @Test
    void blockingOneBandMemberBlocksBothAndAttackerAssignsBlockerDamage() {
        Permanent first = addCreatureReady(player1, new VolunteerReserves());
        Permanent second = addCreatureReady(player1, new VolunteerReserves());
        Permanent blocker = addCreatureReady(player2, new VolunteerReserves());

        declareBand(List.of(0, 1), List.of(List.of(0, 1)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getBlockingTargetIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.passBothPriorities();
        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(first.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Volunteer Reserves");
        harness.assertLife(player2, 20);
    }

    @Test
    void bandingBlockerLetsDefenderAssignAttackerDamageWithoutFormingBand() {
        Permanent attacker = addCreatureReady(player1, new VolunteerReserves());
        Permanent first = addCreatureReady(player2, new VolunteerReserves());
        Permanent second = addCreatureReady(player2, new VolunteerReserves());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(second.getId(), 2));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Volunteer Reserves");
    }

    private void declareBand(List<Integer> attackerIndices, List<List<Integer>> bands) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, attackerIndices, null, bands));
    }
}
