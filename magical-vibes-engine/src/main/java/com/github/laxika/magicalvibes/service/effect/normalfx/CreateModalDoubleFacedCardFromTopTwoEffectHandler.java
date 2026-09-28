package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateModalDoubleFacedCardFromTopTwoEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the one-card-library and two-card-library branches of Blurry Visionary. */
@Component
@RequiredArgsConstructor
public class CreateModalDoubleFacedCardFromTopTwoEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateModalDoubleFacedCardFromTopTwoEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateModalDoubleFacedCardFromTopTwoEffect e =
                (CreateModalDoubleFacedCardFromTopTwoEffect) effect;
        if (e.cardIds().isEmpty()) {
            beginSelection(gameData, entry);
        } else {
            combineSelectedCards(gameData, entry, e.cardIds());
        }
    }

    private void beginSelection(GameData gameData, StackEntry entry) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, 2);
        if (result == null) {
            return;
        }

        List<Card> topCards = result.topCards();
        if (topCards.size() == 1) {
            gameData.addCardToHand(result.controllerId(), topCards.getFirst());
            return;
        }

        String prompt = "Choose which card to put in front of the modal double-faced card.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(result.controllerId(), new ArrayList<>(topCards))
                        .sourceCards(new ArrayList<>(topCards))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.HAND)
                        .followUp(LibrarySearchFollowUp.forSelectedCard(
                                new CardTruePredicate(),
                                new CreateModalDoubleFacedCardFromTopTwoEffect(
                                        topCards.stream().map(Card::getId).toList())))
                        .build(),
                prompt,
                false));
    }

    private void combineSelectedCards(GameData gameData, StackEntry entry, List<UUID> cardIds) {
        Card frontFace = entry.getChosenObjectCard();
        if (frontFace == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Card backFace = gameData.playerDecks.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> cardIds.contains(card.getId()) && !card.getId().equals(frontFace.getId()))
                .findFirst()
                .orElse(null);
        if (backFace == null) {
            return;
        }

        Card combined = frontFace.createRuntimeCopy();
        Card backFaceCopy = backFace.createRuntimeCopy();
        backFaceCopy.setBackFaceCard(null);
        backFaceCopy.setModalDoubleFaced(false);
        combined.setBackFaceCard(backFaceCopy);
        combined.setModalDoubleFaced(true);

        gameData.playerHands.get(controllerId).removeIf(card -> card.getId().equals(frontFace.getId()));
        gameData.playerDecks.get(controllerId).removeIf(card -> card.getId().equals(backFace.getId()));
        gameData.addCardToHand(controllerId, combined);
    }
}
