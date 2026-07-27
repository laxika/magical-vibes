package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IllusionaryForcesTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Illusionary Forces")
    void paysCumulativeUpkeep() {
        Permanent forces = harness.addToBattlefieldAndReturn(player1, new IllusionaryForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(forces.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forces);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Illusionary Forces")
    void declineSacrifices() {
        Permanent forces = harness.addToBattlefieldAndReturn(player1, new IllusionaryForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forces);
        harness.assertInGraveyard(player1, "Illusionary Forces");
    }
}
