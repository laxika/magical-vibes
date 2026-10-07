package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpeciesGorger.class, GrizzlyBears.class, Island.class})
class SpeciesGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep prompts the controller to return a creature they control")
    void upkeepPromptsBounce() {
        UUID gorgerId = harness.addToBattlefieldAndReturn(player1, new SpeciesGorger()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefield(player1, new Island());

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
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

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
        UUID gorgerId = harness.addToBattlefieldAndReturn(player1, new SpeciesGorger()).getId();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, gorgerId);

        harness.assertNotOnBattlefield(player1, "Species Gorger");
        harness.assertInHand(player1, "Species Gorger");
    }

    @Test
    @DisplayName("Creatures the opponent controls are never choices")
    void opponentCreaturesNotChoices() {
        UUID gorgerId = harness.addToBattlefieldAndReturn(player1, new SpeciesGorger()).getId();
        harness.addToBattlefield(player2, new GrizzlyBears());

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

    @Test
    @DisplayName("A creature owned by the opponent returns to the opponent's hand")
    void borrowedCreatureReturnsToOwner() {
        harness.addToBattlefield(player1, new SpeciesGorger());
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player2.getId());
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, bears).getId();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Species Gorger");
    }

    @Test
    @DisplayName("The trigger still returns a creature after Species Gorger leaves")
    void triggerResolvesWithoutSource() {
        UUID gorgerId = harness.addToBattlefieldAndReturn(player1, new SpeciesGorger()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(gorgerId));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bearsId);
        harness.handlePermanentChosen(player1, bearsId);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger does nothing if no creatures remain when it resolves")
    void noCreaturesAtResolution() {
        harness.addToBattlefield(player1, new SpeciesGorger());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof SpeciesGorger);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
