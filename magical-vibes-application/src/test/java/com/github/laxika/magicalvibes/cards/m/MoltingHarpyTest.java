package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoltingHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} at upkeep keeps Molting Harpy on the battlefield")
    void payingUpkeepCostKeepsIt() {
        harness.addToBattlefield(player1, new MoltingHarpy());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Molting Harpy");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining to pay at upkeep sacrifices Molting Harpy")
    void decliningUpkeepPaymentSacrificesIt() {
        harness.addToBattlefield(player1, new MoltingHarpy());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Molting Harpy");
    }

    @Test
    @DisplayName("Accepting without enough mana sacrifices Molting Harpy")
    void notEnoughManaSacrificesIt() {
        harness.addToBattlefield(player1, new MoltingHarpy());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Molting Harpy");
    }

    @Test
    @DisplayName("Molting Harpy does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new MoltingHarpy());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Molting Harpy");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
