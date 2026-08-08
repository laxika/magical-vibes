package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpeciesGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep prompts the controller to return a creature they control")
    void upkeepPromptsBounce() {
        harness.addToBattlefield(player1, new SpeciesGorger());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        UUID gorgerId = harness.getPermanentId(player1, "Species Gorger");
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(gorgerId, bearsId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen creature is returned to its owner's hand")
    void chosenCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new SpeciesGorger());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Species Gorger");
    }

    @Test
    @DisplayName("With no other creature the Gorger must return itself")
    void aloneReturnsItself() {
        harness.addToBattlefield(player1, new SpeciesGorger());
        UUID gorgerId = harness.getPermanentId(player1, "Species Gorger");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, gorgerId);

        harness.assertNotOnBattlefield(player1, "Species Gorger");
        harness.assertInHand(player1, "Species Gorger");
    }

    @Test
    @DisplayName("Creatures the opponent controls are never choices")
    void opponentCreaturesNotChoices() {
        harness.addToBattlefield(player1, new SpeciesGorger());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID gorgerId = harness.getPermanentId(player1, "Species Gorger");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(gorgerId);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger does not fire on the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new SpeciesGorger());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Species Gorger");
    }
}
