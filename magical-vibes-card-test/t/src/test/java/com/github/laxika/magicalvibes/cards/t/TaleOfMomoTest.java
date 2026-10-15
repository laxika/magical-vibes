package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AangAirNomad;
import com.github.laxika.magicalvibes.cards.a.AardvarkSloth;
import com.github.laxika.magicalvibes.cards.o.OppositionAgent;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaleOfMomo.class, AangAirNomad.class, Plains.class, AardvarkSloth.class,
        PsychogenicProbe.class, OppositionAgent.class})
class TaleOfMomoTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the library and graveyard for an Ally creature")
    void searchesForAllyCreature() {
        Card libraryAlly = new AangAirNomad();
        Card graveyardAlly = new AangAirNomad();
        Card libraryNonAlly = new AardvarkSloth();
        Card graveyardNonAlly = new AardvarkSloth();
        harness.setLibrary(player1, List.of(libraryAlly, libraryNonAlly));
        harness.setGraveyard(player1, List.of(graveyardAlly, graveyardNonAlly));
        castWithFullCost();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(libraryAlly.getId(), graveyardAlly.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardAlly.getId()));

        harness.assertInHand(player1, "Aang, Air Nomad");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardAlly);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(libraryAlly, libraryNonAlly);
    }

    @Test
    @DisplayName("Costs two less after a creature leaves under your control")
    void costsTwoLessAfterCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AardvarkSloth());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.setLibrary(player1, List.of(new AangAirNomad()));
        harness.castFromHand(player1, new TaleOfMomo(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class))
                .isNotNull();
    }

    @Test
    @DisplayName("A creature dying under your control enables the discount")
    void costsOnlyWhiteAfterCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AardvarkSloth());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.setLibrary(player1, List.of(new AangAirNomad()));

        harness.castFromHand(player1, new TaleOfMomo(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class))
                .isNotNull();
        harness.assertInGraveyard(player1, "Aardvark Sloth");
    }

    @Test
    @DisplayName("A creature being exiled under your control enables the discount")
    void costsOnlyWhiteAfterCreatureIsExiled() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AardvarkSloth());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, creature));
        harness.setLibrary(player1, List.of(new AangAirNomad()));

        harness.castFromHand(player1, new TaleOfMomo(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class))
                .isNotNull();
        harness.assertNotOnBattlefield(player1, "Aardvark Sloth");
        harness.assertNotInGraveyard(player1, "Aardvark Sloth");
    }

    @Test
    @DisplayName("Does not reduce the cost after only a land leaves")
    void doesNotReduceCostAfterLandLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        harness.setHand(player1, List.of(new TaleOfMomo()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not reduce the cost after an opponent's creature leaves")
    void doesNotReduceCostAfterOpponentsCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AardvarkSloth());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.setHand(player1, List.of(new TaleOfMomo()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An Ally found in the library is revealed, put into hand, and the library is shuffled")
    @CardUsed({PsychogenicProbe.class})
    void takesLibraryAllyAndShuffles() {
        Card ally = new AangAirNomad();
        Card land = new Plains();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLibrary(player1, List.of(ally, land));
        harness.setGraveyard(player1, List.of());
        castWithFullCost();

        harness.handleMultipleCardsChosen(player1, List.of(ally.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ally);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals Aang, Air Nomad"));
        harness.assertInGraveyard(player1, "Tale of Momo");
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Searching only the graveyard does not shuffle the library or trigger Probe")
    @CardUsed({PsychogenicProbe.class})
    void graveyardOnlySearchDoesNotShuffle() {
        Card ally = new AangAirNomad();
        Card land = new Plains();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLibrary(player1, List.of(land));
        harness.setGraveyard(player1, List.of(ally));
        castWithFullCost();

        harness.handleMultipleCardsChosen(player1, List.of(ally.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ally);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertNotInGraveyard(player1, "Aang, Air Nomad");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A library search may fail to find a matching Ally but still shuffles")
    @CardUsed({PsychogenicProbe.class})
    void mayFailToFindInLibrary() {
        Card ally = new AangAirNomad();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLibrary(player1, List.of(ally));
        harness.setGraveyard(player1, List.of());
        castWithFullCost();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ally);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An opponent's Opposition Agent controls the library search and exiles the found Ally")
    @CardUsed({OppositionAgent.class})
    void oppositionAgentControlsLibrarySearch() {
        Card ally = new AangAirNomad();
        harness.addToBattlefield(player2, new OppositionAgent());
        harness.setLibrary(player1, List.of(ally));
        harness.setGraveyard(player1, List.of());
        castWithFullCost();

        PendingInteraction interaction = gd.interaction.activeInteraction();
        assertThat(interaction).isNotNull();
        assertThat(interaction.decidingPlayerId()).isEqualTo(player2.getId());

        if (interaction instanceof PendingInteraction.LibrarySearch) {
            harness.handleCardChosen(player2, 0);
        } else {
            harness.handleMultipleCardsChosen(player2, List.of(ally.getId()));
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ally);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ally);
        assertThat(gd.exilePlayPermissions.get(ally.getId())).isEqualTo(player2.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(ally.getId());
    }

    @Test
    @DisplayName("Search excludes lands, non-Allies, and opposing zones")
    void excludesNoncreaturesAndOpponentsCards() {
        Card ally = new AangAirNomad();
        Card libraryLand = new Plains();
        Card graveyardLand = new Plains();
        Card nonAlly = new AardvarkSloth();
        Card opposingLibraryAlly = new AangAirNomad();
        Card opposingGraveyardAlly = new AangAirNomad();
        harness.setLibrary(player1, List.of(libraryLand, nonAlly, ally));
        harness.setGraveyard(player1, List.of(graveyardLand));
        harness.setLibrary(player2, List.of(opposingLibraryAlly));
        harness.setGraveyard(player2, List.of(opposingGraveyardAlly));
        castWithFullCost();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ally.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ally.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ally);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingLibraryAlly);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingGraveyardAlly);
    }

    @Test
    @DisplayName("The combined search can take only one Ally total")
    void cannotTakeOneAllyFromEachZone() {
        Card libraryAlly = new AangAirNomad();
        Card graveyardAlly = new AangAirNomad();
        harness.setLibrary(player1, List.of(libraryAlly));
        harness.setGraveyard(player1, List.of(graveyardAlly));
        castWithFullCost();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(libraryAlly.getId(), graveyardAlly.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryAlly);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardAlly);
        harness.handleMultipleCardsChosen(player1, List.of(libraryAlly.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryAlly);
    }

    @Test
    @DisplayName("The spell resolves without a selection when both search zones are empty")
    void resolvesWithEmptySearchZones() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        castWithFullCost();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Tale of Momo");
    }

    @Test
    @DisplayName("Multiple creatures leaving never reduce the white mana requirement")
    void multipleDeparturesDoNotRemoveWhiteCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AardvarkSloth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AardvarkSloth());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToHand(gd, second);
        });
        harness.setHand(player1, List.of(new TaleOfMomo()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature leaving on a previous turn does not reduce the cost")
    void discountExpiresAtTurnBoundary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AardvarkSloth());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.setLibrary(player2, List.of(new Plains()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Plains()));
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TaleOfMomo()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithFullCost() {
        harness.castFromHand(player1, new TaleOfMomo(), "{2}{W}");
        harness.passBothPriorities();
    }
}
