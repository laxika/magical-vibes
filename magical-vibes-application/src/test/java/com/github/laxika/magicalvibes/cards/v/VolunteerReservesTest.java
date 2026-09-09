package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void declareBand(List<Integer> attackerIndices, List<List<Integer>> bands) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, attackerIndices, null, bands));
    }
}
