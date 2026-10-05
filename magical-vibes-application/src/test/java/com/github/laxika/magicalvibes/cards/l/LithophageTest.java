package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lithophage.class, Mountain.class, Forest.class})
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
    @DisplayName("A non-Mountain land does not satisfy the upkeep cost")
    void nonMountainLandDoesNotSatisfyUpkeepCost() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lithophage");
        harness.assertInGraveyard(player1, "Lithophage");
        harness.assertOnBattlefield(player1, "Forest");
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

    @Test
    @DisplayName("Lithophage does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lithophage());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Lithophage");
        harness.assertNotInGraveyard(player1, "Lithophage");
    }

    @Test
    @DisplayName("The controller chooses exactly one Mountain to sacrifice")
    void choosesExactlyOneMountain() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        var mountains = findPermanents(player1, "Mountain");
        var chosenMountain = mountains.get(1);

        advanceToUpkeep(player1);
        chosenMountain.setTapped(true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, chosenMountain.getId());

        harness.assertOnBattlefield(player1, "Lithophage");
        assertThat(findPermanents(player1, "Mountain"))
                .extracting(permanent -> permanent.getId())
                .containsExactly(mountains.get(0).getId());
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
