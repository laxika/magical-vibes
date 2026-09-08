package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PitRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {2}{B}{B} sacrifices Pit Raptor")
    void decliningPaymentSacrifices() {
        harness.addToBattlefield(player1, new PitRaptor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Pit Raptor");
        harness.assertInGraveyard(player1, "Pit Raptor");
    }

    @Test
    @DisplayName("Paying {2}{B}{B} keeps Pit Raptor and spends the mana")
    void payingKeepsCreature() {
        harness.addToBattlefield(player1, new PitRaptor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Pit Raptor");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana still sacrifices Pit Raptor")
    void acceptWithoutManaSacrifices() {
        harness.addToBattlefield(player1, new PitRaptor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Pit Raptor");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PitRaptor());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pit Raptor");
    }
}
