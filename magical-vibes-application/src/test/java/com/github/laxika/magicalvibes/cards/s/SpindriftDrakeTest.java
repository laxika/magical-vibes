package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpindriftDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {U} sacrifices Spindrift Drake")
    void declineSacrifices() {
        harness.addToBattlefield(player1, new SpindriftDrake());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Spindrift Drake");
        harness.assertInGraveyard(player1, "Spindrift Drake");
    }

    @Test
    @DisplayName("Paying {U} keeps Spindrift Drake on the battlefield")
    void payKeeps() {
        harness.addToBattlefield(player1, new SpindriftDrake());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Spindrift Drake");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Accepting without blue mana still sacrifices Spindrift Drake")
    void acceptWithoutManaSacrifices() {
        harness.addToBattlefield(player1, new SpindriftDrake());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Spindrift Drake");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new SpindriftDrake());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spindrift Drake");
    }
}
