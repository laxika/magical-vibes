package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BidLifeEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.model.InteractionShape;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.event.InteractionPromptProjectionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SpecializedInteractionProjectionTest {

    private UUID controllerId;
    private UUID opponentId;
    private GameData gameData;
    private InteractionPromptProjectionRegistry projections;

    @BeforeEach
    void setUp() {
        controllerId = UUID.randomUUID();
        opponentId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "specialized-projection", controllerId, "Alice");
        gameData.playerIdToName.put(controllerId, "Alice");
        gameData.playerIds.add(opponentId);
        gameData.orderedPlayerIds.add(opponentId);
        gameData.playerIdToName.put(opponentId, "Bob");
        gameData.playerHands.put(opponentId, new ArrayList<>());
        projections = new InteractionPromptProjectionRegistry(mock(CardViewFactory.class));
    }

    @Test
    @DisplayName("Whether to search a library is asked as an accept/decline prompt naming the search")
    void maySearchLibraryChoiceIsAnAcceptDecline() {
        InteractionPromptMessage message = prompt(new PendingInteraction.MaySearchLibraryChoice(controllerId,
                LibrarySearchParams.builder(controllerId, List.of()).canFailToFind(true).build(),
                "Search your library for up to 2 basic land cards."));

        assertThat(message.shape()).isEqualTo(InteractionShape.ACCEPT_DECLINE);
        assertThat(message.prompt()).isEqualTo(
                "Do you search? Search your library for up to 2 basic land cards.");
    }

    @Test
    @DisplayName("An and/or search's zones are asked as an accept/decline prompt naming the other zones")
    void andOrSearchZonesChoiceIsAnAcceptDecline() {
        InteractionPromptMessage message = prompt(new PendingInteraction.AndOrSearchZonesChoice(
                new PendingInteraction.SearchLibraryAndOrGraveyardChoice(controllerId, List.of(), Set.of(), true,
                        "Dragon card"),
                Set.of(Zone.HAND, Zone.GRAVEYARD)));

        assertThat(message.shape()).isEqualTo(InteractionShape.ACCEPT_DECLINE);
        assertThat(message.prompt()).isEqualTo(
                "Do you search your library for a Dragon card? If you don't, you search only your hand and your"
                        + " graveyard.");
    }

    @Test
    void brilliantUltimatumPileAssignmentPileChoiceAndPlayChoiceAreRegistryProjected() {
        Card first = card("First");
        Card second = card("Second");
        gameData.addToExile(controllerId, first);
        gameData.addToExile(controllerId, second);

        InteractionPromptMessage separation = prompt(
                new PendingInteraction.BrilliantUltimatumPileSeparationChoice(
                        opponentId, List.of(first.getId(), second.getId())));
        InteractionPromptMessage pileChoice = prompt(
                new PendingInteraction.BrilliantUltimatumPileChoice(
                        controllerId, List.of(first.getId()), List.of(second.getId())));
        InteractionPromptMessage playChoice = prompt(
                new PendingInteraction.BrilliantUltimatumPlayChoice(
                        controllerId, List.of(first.getId()), 1));

        assertThat(separation.shape()).isEqualTo(InteractionShape.MULTI_CARD_PICK);
        assertThat(separation.prompt()).contains("Pile 1", "Pile 2");
        assertThat(pileChoice.shape()).isEqualTo(InteractionShape.ACCEPT_DECLINE);
        assertThat(pileChoice.prompt()).contains("Pile 1 (First)", "Pile 2 (Second)");
        assertThat(playChoice.shape()).isEqualTo(InteractionShape.MULTI_CARD_PICK);
        assertThat(playChoice.prompt()).contains("play lands and cast spells");
    }

    @Test
    @DisplayName("A searched hand is shown whole, and a library that wasn't searched not at all")
    void sameNameSearchShowsExactlyTheSearchedZones() {
        Card match = card("Match");
        Card otherInHand = card("Other in hand");
        Card inLibrary = card("In library");
        gameData.playerHands.put(opponentId, new ArrayList<>(List.of(match, otherInHand)));
        gameData.playerDecks.put(opponentId, new ArrayList<>(List.of(inLibrary)));

        InteractionPromptMessage search = prompt(new PendingInteraction.MultiZoneExileChoice(
                controllerId, List.of(match.getId()), 1, opponentId, controllerId, "Match", null,
                null, false,
                new PendingInteraction.MultiZoneExileChoice.SearchedZones(Set.of(Zone.HAND), 0), false, true));

        assertThat(search.cardIds()).containsExactly(match.getId(), null);
        assertThat(search.cards()).hasSize(2);
        assertThat(search.maxCount()).isEqualTo(1);
    }

    @Test
    void auctionPromptsAreDerivedFromInteractionFactsAndCurrentIdentities() {
        Card targetCard = card("Auction Target");
        Permanent target = new Permanent(targetCard);
        gameData.playerBattlefields.put(controllerId, new ArrayList<>(List.of(target)));

        InteractionPromptMessage permanentAuction = prompt(
                new PendingInteraction.PermanentAuctionChoice(
                        controllerId, List.of(), List.of(controllerId), List.of()));
        InteractionPromptMessage illicitAuction = prompt(
                new PendingInteraction.LifeBidChoice(
                        opponentId,
                        4,
                        999,
                        "Illicit Auction",
                        controllerId,
                        false,
                        BidLifeEffect.Prize.CONTROL_OF_TARGET,
                        target.getId()));

        assertThat(permanentAuction.prompt())
                .isEqualTo("Choose one of the auctioned cards to put onto the battlefield tapped "
                        + "under your control.");
        assertThat(illicitAuction.shape()).isEqualTo(InteractionShape.NUMBER_PICK);
        assertThat(illicitAuction.prompt())
                .contains("Auction Target", "current high bid: 4 by Alice");
    }

    @Test
    void revealThenDiscardPromptsAreDerivedForBothStages() {
        InteractionPromptMessage initialReveal = prompt(
                new PendingInteraction.RevealCardsDiscardChoice(
                        opponentId,
                        opponentId,
                        controllerId,
                        true,
                        List.of(0, 1),
                        2,
                        List.of(),
                        1,
                        HandChoiceDestination.DISCARD));
        InteractionPromptMessage continuedReveal = prompt(
                new PendingInteraction.RevealCardsDiscardChoice(
                        opponentId,
                        opponentId,
                        controllerId,
                        true,
                        List.of(1),
                        1,
                        List.of(UUID.randomUUID()),
                        1,
                        HandChoiceDestination.DISCARD));
        InteractionPromptMessage discard = prompt(
                new PendingInteraction.RevealCardsDiscardChoice(
                        controllerId,
                        opponentId,
                        controllerId,
                        false,
                        List.of(0),
                        1,
                        List.of(),
                        1,
                        HandChoiceDestination.DISCARD));

        assertThat(initialReveal.prompt()).isEqualTo("Choose 2 cards to reveal.");
        assertThat(continuedReveal.prompt()).isEqualTo("Choose another card to reveal.");
        assertThat(discard.prompt()).isEqualTo("Choose a card for Bob to discard.");
    }

    @Test
    void activatedAbilityGraveyardExileChoiceProjectsItsRequiredSelectionRange() {
        Card first = card("First");
        Card second = card("Second");
        Card third = card("Third");

        InteractionPromptMessage message = prompt(
                new PendingInteraction.ActivatedAbilityGraveyardExileCostChoice(
                        controllerId, UUID.randomUUID(), 0, null, null,
                        List.of(first, second, third), "Choose two cards.", 2, 2, false));

        assertThat(message.shape()).isEqualTo(InteractionShape.MULTI_CARD_PICK);
        assertThat(message.minCount()).isEqualTo(2);
        assertThat(message.maxCount()).isEqualTo(2);
        assertThat(message.cardIds()).containsExactly(first.getId(), second.getId(), third.getId());
    }

    @Test
    @DisplayName("A graveyard pick over a card pool sends the pool the indices address, an own-graveyard pick does not")
    void graveyardChoiceOverAPoolSendsThePool() {
        Card first = card("First");
        Card second = card("Second");
        gameData.playerGraveyards.put(controllerId, new ArrayList<>(List.of(first)));
        gameData.playerGraveyards.put(opponentId, new ArrayList<>(List.of(second)));

        InteractionPromptMessage overPool = prompt(PendingInteraction.GraveyardChoice
                .builder(controllerId, List.of(1), GraveyardChoiceDestination.HAND, "Choose.")
                .cardPool(List.of(first, second))
                .build());
        InteractionPromptMessage ownGraveyard = prompt(PendingInteraction.GraveyardChoice
                .builder(controllerId, List.of(1), GraveyardChoiceDestination.HAND, "Choose.")
                .build());

        assertThat(overPool.shape()).isEqualTo(InteractionShape.GRAVEYARD_INDEX_PICK);
        assertThat(overPool.allGraveyards()).isTrue();
        assertThat(overPool.cardIndices()).containsExactly(1);
        assertThat(overPool.cards()).hasSize(2);
        assertThat(ownGraveyard.allGraveyards()).isFalse();
        assertThat(ownGraveyard.cards()).isNull();
    }

    @Test
    @DisplayName("A mandatory pick among cards tells the client how many have to be chosen")
    void mandatoryMultiGraveyardChoiceProjectsItsMinimum() {
        InteractionPromptMessage exactlyTwo = prompt(new PendingInteraction.MultiGraveyardChoice(
                controllerId, List.of(card("First"), card("Second"), card("Third")), 2, "Choose two cards.", 2));
        InteractionPromptMessage upToTwo = prompt(new PendingInteraction.MultiGraveyardChoice(
                controllerId, List.of(card("First"), card("Second")), 2, "Choose up to two cards."));

        assertThat(exactlyTwo.minCount()).isEqualTo(2);
        assertThat(exactlyTwo.maxCount()).isEqualTo(2);
        assertThat(upToTwo.minCount()).isIn(null, 0);
    }

    @Test
    @DisplayName("Every card pick is projected with the bounds its answer is checked against")
    void cardPicksProjectTheirBounds() {
        Card first = card("First");
        Card second = card("Second");
        List<UUID> ids = List.of(first.getId(), second.getId());

        InteractionPromptMessage optionalPlay = prompt(new PendingInteraction.ImprovisationCapstoneCastChoice(
                controllerId, ids, 1, "You may play one of the exiled cards without paying its mana cost."));
        InteractionPromptMessage choosePlayable = prompt(new PendingInteraction.ExiledCardMayPlayChoice(
                controllerId, ids, true, false, true));
        InteractionPromptMessage mayCopy = prompt(
                new PendingInteraction.ExiledSpellCopyChoice(controllerId, ids, 1));
        InteractionPromptMessage searchForTwo = prompt(new PendingInteraction.SearchLibraryAndOrGraveyardChoice(
                controllerId, List.of(first, second), Set.of(first.getId(), second.getId()), true, "land card",
                com.github.laxika.magicalvibes.model.LibrarySearchDestination.HAND, 2));
        InteractionPromptMessage mustFindInGraveyard = prompt(new PendingInteraction.SearchLibraryAndOrGraveyardChoice(
                controllerId, List.of(first, second), Set.of(first.getId()), true, "land card"));

        assertThat(optionalPlay.maxCount()).isEqualTo(1);
        assertThat(optionalPlay.minCount()).isIn(null, 0);
        assertThat(choosePlayable.maxCount()).isEqualTo(1);
        assertThat(choosePlayable.minCount()).isEqualTo(1);
        assertThat(mayCopy.minCount()).isIn(null, 0);
        assertThat(searchForTwo.maxCount()).isEqualTo(2);
        assertThat(mustFindInGraveyard.minCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("No prompt over exiled cards shows a face-down one to a player who may not look at it (CR 406.3)")
    void exiledCardPromptsHideFaceDownCards() {
        Card hidden = card("Hidden");
        gameData.addToExile(opponentId, hidden, UUID.randomUUID(), true);
        gameData.setExiledCardCounters(hidden.getId(), CounterType.TIME, 1);
        List<UUID> ids = List.of(hidden.getId());

        List<PendingInteraction> choices = List.of(
                new PendingInteraction.ImprovisationCapstoneCastChoice(controllerId, ids, 1),
                new PendingInteraction.ExiledSpellCopyChoice(controllerId, ids, 1),
                new PendingInteraction.ExiledCardMayPlayChoice(controllerId, ids),
                new PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice(
                        controllerId, UUID.randomUUID(), 0, 0, null, null, List.of(), java.util.Map.of(), ids),
                new PendingInteraction.SuspendedCardTimeCounterChoice(controllerId, ids, 1, "Source"),
                new PendingInteraction.BrilliantUltimatumPlayChoice(controllerId, ids, 1),
                new PendingInteraction.BrilliantUltimatumPileSeparationChoice(controllerId, ids));

        for (PendingInteraction choice : choices) {
            assertThat(prompt(choice).cards()).as(choice.getClass().getSimpleName())
                    .singleElement().satisfies(view -> assertThat(view).isNotNull()
                            .extracting(shown -> shown.name()).isEqualTo("Face-down card"));
        }
        assertThat(prompt(new PendingInteraction.BrilliantUltimatumPileChoice(controllerId, ids, List.of())).prompt())
                .doesNotContain("Hidden");
    }

    @Test
    @DisplayName("An index pick over a card pool sends a face-down placeholder for a card exiled face down (CR 406.3)")
    void pooledIndexPickSendsAPlaceholderForAHiddenCard() {
        Card hidden = card("Hidden");
        Card shown = card("Shown");
        gameData.addToExile(opponentId, hidden, UUID.randomUUID(), true);
        gameData.addToExile(opponentId, shown, UUID.randomUUID(), false);

        InteractionPromptMessage message = prompt(PendingInteraction.GraveyardChoice
                .builder(controllerId, List.of(0, 1), GraveyardChoiceDestination.BATTLEFIELD, "Choose a card.")
                .cardPool(List.of(hidden, shown))
                .build());

        assertThat(message.cards()).hasSize(2);
        assertThat(message.cards().getFirst().name()).isEqualTo("Face-down card");
        assertThat(message.cards().getFirst().id()).isEqualTo(hidden.getId());
        assertThat(message.prompt()).doesNotContain("Hidden");
    }

    private InteractionPromptMessage prompt(PendingInteraction interaction) {
        return (InteractionPromptMessage) projections.project(gameData, interaction).orElseThrow();
    }

    private static Card card(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
