package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
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
                controllerId, List.of(match.getId()), 1, opponentId, controllerId, "Match", false, null, null, null,
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
                new PendingInteraction.IllicitAuctionBidChoice(
                        opponentId,
                        4,
                        999,
                        "Illicit Auction",
                        target.getId(),
                        controllerId));

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

    private InteractionPromptMessage prompt(PendingInteraction interaction) {
        return (InteractionPromptMessage) projections.project(gameData, interaction).orElseThrow();
    }

    private static Card card(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
