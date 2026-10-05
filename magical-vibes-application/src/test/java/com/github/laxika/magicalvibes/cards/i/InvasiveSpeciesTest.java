package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasiveSpecies.class, Island.class, RuneclawBear.class})
class InvasiveSpeciesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering prompts the controller to return another permanent they control")
    void etbPromptsBounceOfAnotherPermanent() {
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");

        harness.castFromHand(player1, new InvasiveSpecies(), "{2}{G}");
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(islandId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen permanent is returned to its owner's hand")
    void chosenPermanentReturnsToHand() {
        addCreatureReady(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        harness.castFromHand(player1, new InvasiveSpecies(), "{2}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInHand(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Invasive Species");
    }

    @Test
    @DisplayName("Permanents the opponent controls are never returned")
    void opponentPermanentsNotChoices() {
        addCreatureReady(player2, new RuneclawBear());

        harness.castFromHand(player1, new InvasiveSpecies(), "{2}{G}");
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("With no other permanent, nothing happens and it does not bounce itself")
    void noOtherPermanentNoBounce() {
        harness.castFromHand(player1, new InvasiveSpecies(), "{2}{G}");
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Invasive Species");
    }

    @Test
    @DisplayName("A land can be returned, not just a creature")
    void returnsChosenLand() {
        var land = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.castFromHand(player1, new InvasiveSpecies(), "{2}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, land.getId());

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Invasive Species");
    }

    @Test
    @DisplayName("Another copy of Invasive Species is a valid choice")
    void canReturnAnotherCopy() {
        var other = harness.addToBattlefieldAndReturn(player1, new InvasiveSpecies());

        harness.castFromHand(player1, new InvasiveSpecies(), "{2}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(other.getId());
        harness.handlePermanentChosen(player1, other.getId());

        assertThat(findPermanents(player1, "Invasive Species"))
                .hasSize(1).doesNotContain(other);
        harness.assertInHand(player1, "Invasive Species");
    }

    @Test
    @DisplayName("A controlled permanent owned by the opponent returns to the opponent's hand")
    void returnsPermanentToItsOwnerRatherThanItsController() {
        RuneclawBear borrowed = new RuneclawBear();
        borrowed.setOwnerId(player2.getId());
        var permanent = harness.addToBattlefieldAndReturn(player1, borrowed);

        harness.castFromHand(player1, new InvasiveSpecies(), "{2}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, permanent.getId());

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInHand(player2, "Runeclaw Bear");
        harness.assertNotInHand(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Invasive Species");
    }
}
