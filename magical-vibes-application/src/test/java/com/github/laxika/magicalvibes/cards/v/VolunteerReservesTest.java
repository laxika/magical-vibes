package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
