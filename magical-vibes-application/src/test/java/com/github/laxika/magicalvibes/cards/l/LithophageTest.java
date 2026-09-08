package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LithophageTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep without a Mountain sacrifices Lithophage")
    void upkeepWithoutMountainSacrificesLithophage() {
        harness.addToBattlefield(player1, new Lithophage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lithophage");
        harness.assertInGraveyard(player1, "Lithophage");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Upkeep with a Mountain asks whether to sacrifice it")
    void upkeepWithMountainPromptsForSacrifice() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Sacrificing a Mountain keeps Lithophage")
    void sacrificingMountainKeepsLithophage() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Mountain").getId());

        harness.assertOnBattlefield(player1, "Lithophage");
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Declining to sacrifice a Mountain sacrifices Lithophage")
    void decliningMountainSacrificeSacrificesLithophage() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Lithophage");
        harness.assertInGraveyard(player1, "Lithophage");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("An opponent's Mountain does not satisfy the upkeep cost")
    void opponentMountainDoesNotSatisfyUpkeepCost() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player2, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lithophage");
        harness.assertInGraveyard(player1, "Lithophage");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
