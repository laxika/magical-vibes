package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PhantasmalForces.class)
class PhantasmalForcesTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {U} sacrifices Phantasmal Forces")
    void decliningPaymentSacrificesCreature() {
        harness.addToBattlefield(player1, new PhantasmalForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Phantasmal Forces");
        harness.assertInGraveyard(player1, "Phantasmal Forces");
    }

    @Test
    @DisplayName("Paying {U} keeps Phantasmal Forces on the battlefield")
    void payingKeepsCreature() {
        harness.addToBattlefield(player1, new PhantasmalForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Phantasmal Forces");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void acceptingWithOnlyNonBlueManaSacrificesCreature() {
        harness.addToBattlefield(player1, new PhantasmalForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Phantasmal Forces");
        harness.assertInGraveyard(player1, "Phantasmal Forces");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PhantasmalForces());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phantasmal Forces");
    }
}
