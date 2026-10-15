package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DazzlingLights;
import com.github.laxika.magicalvibes.cards.d.DimirGuildgate;
import com.github.laxika.magicalvibes.cards.o.OrneryGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnhancedSurveillance.class, DazzlingLights.class, OrneryGoblin.class, DimirGuildgate.class})
class EnhancedSurveillanceTest extends BaseCardTest {

    @Test
    @DisplayName("May look at two additional cards while surveilling")
    void mayLookAtAdditionalCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new OrneryGoblin());
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        Card first = new OrneryGoblin();
        Card second = new DimirGuildgate();
        Card third = new OrneryGoblin();
        Card fourth = new DimirGuildgate();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second, third, fourth);
    }

    @Test
    @DisplayName("May decline to look at additional cards")
    void mayDeclineAdditionalCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new OrneryGoblin());
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        Card first = new OrneryGoblin();
        Card second = new DimirGuildgate();
        Card third = new OrneryGoblin();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second);
    }

    @Test
    @DisplayName("Exiles itself and shuffles its controller's graveyard into their library")
    void exilesSelfAndShufflesGraveyardIntoLibrary() {
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        harness.setGraveyard(player1, List.of(new OrneryGoblin(), new DimirGuildgate()));
        harness.setLibrary(player1, List.of(new OrneryGoblin()));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore + 2);
        harness.assertNotOnBattlefield(player1, "Enhanced Surveillance");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Enhanced Surveillance"));
    }

    @Test
    void additionalCardsCanGoToGraveyardAndBeReordered() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrneryGoblin());
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        Card first = new OrneryGoblin();
        Card second = new DimirGuildgate();
        Card third = new DazzlingLights();
        Card fourth = new EnhancedSurveillance();
        Card fifth = new DimirGuildgate();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(3, 0), List.of(1, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, first, fifth);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second, third);
    }

    @Test
    void twoCopiesEachAllowTwoAdditionalCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrneryGoblin());
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        List<Card> library = List.of(new OrneryGoblin(), new DimirGuildgate(),
                new DazzlingLights(), new EnhancedSurveillance(), new OrneryGoblin(),
                new DimirGuildgate(), new DazzlingLights());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactlyElementsOf(library.subList(0, 6));
    }

    @Test
    void canDeclineOneCopyAndAcceptTheOther() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrneryGoblin());
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        List<Card> library = List.of(new OrneryGoblin(), new DimirGuildgate(),
                new DazzlingLights(), new EnhancedSurveillance(), new OrneryGoblin());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactlyElementsOf(library.subList(0, 4));
    }

    @Test
    void additionalCardsAreLimitedByTheRemainingLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrneryGoblin());
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        Card first = new OrneryGoblin();
        Card second = new DimirGuildgate();
        Card third = new EnhancedSurveillance();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third);
    }

    @Test
    void opponentsSurveilDoesNotReceiveAdditionalCards() {
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrneryGoblin());
        Card first = new OrneryGoblin();
        Card second = new DimirGuildgate();
        harness.setLibrary(player2, List.of(first, second, new DazzlingLights()));
        harness.setHand(player2, List.of(new DazzlingLights()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
    }

    @Test
    void exileCostIsPaidBeforeResolutionAndOnlyControllersGraveyardIsShuffled() {
        Card surveillance = new EnhancedSurveillance();
        Card ownGraveyardCard = new OrneryGoblin();
        Card opponentGraveyardCard = new DimirGuildgate();
        Card libraryCard = new DazzlingLights();
        harness.addToBattlefield(player1, surveillance);
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Enhanced Surveillance");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(surveillance);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownGraveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryCard, ownGraveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(surveillance);
    }
}
