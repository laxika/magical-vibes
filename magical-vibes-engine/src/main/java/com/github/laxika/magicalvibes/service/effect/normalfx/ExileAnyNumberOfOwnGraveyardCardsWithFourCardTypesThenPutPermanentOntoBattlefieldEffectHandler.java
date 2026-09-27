package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingReturnExiledWithSourceCard;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfOwnGraveyardCardsWithFourCardTypesThenPutPermanentOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Winter's delirium graveyard exile and permanent return. */
@Component
@RequiredArgsConstructor
public class ExileAnyNumberOfOwnGraveyardCardsWithFourCardTypesThenPutPermanentOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final GraveyardService graveyardService;
    private final PlayerInputService playerInputService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameMutationCoordinator mutationCoordinator;
    private final ReturnCardExiledWithSourceToBattlefieldEffectHandler returnHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAnyNumberOfOwnGraveyardCardsWithFourCardTypesThenPutPermanentOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var winterEffect =
                (ExileAnyNumberOfOwnGraveyardCardsWithFourCardTypesThenPutPermanentOntoBattlefieldEffect) effect;
        var state = gameData.graveyardTargetOperation;
        UUID controllerId = entry.getControllerId();

        if (state.resolutionTimeExileAnyNumberWithFourCardTypesChoiceMade) {
            List<UUID> chosenCardIds = state.resolutionTimeExileAnyNumberWithFourCardTypesChosenCardIds;
            state.resolutionTimeExileAnyNumberWithFourCardTypesResume = false;
            state.resolutionTimeExileAnyNumberWithFourCardTypesChoiceMade = false;
            state.resolutionTimeExileAnyNumberWithFourCardTypesChosenCardIds = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            List<Card> selectedCards = cardsStillInGraveyard(gameData, controllerId, chosenCardIds);
            if (selectedCards.isEmpty()) {
                return;
            }
            if (cardTypes(selectedCards).size() < 4) {
                beginChoice(gameData, entry, matchingGraveyardCards(gameData, entry));
                return;
            }

            List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
            graveyard.removeAll(selectedCards);
            graveyardService.notifyCardsExiledFromGraveyard(gameData, controllerId, selectedCards);
            for (Card card : selectedCards) {
                exileService.exileCard(gameData, controllerId, card);
                gameLogService.append(gameData,
                        GameLog.textCardText(entry.getCard().getName() + " exiles ", card,
                                " from its controller's graveyard."));
            }

            List<Card> permanentCards = selectedCards.stream()
                    .filter(this::isPermanentCard)
                    .toList();
            if (permanentCards.isEmpty()) {
                return;
            }
            if (permanentCards.size() == 1) {
                returnHandler.returnToBattlefield(gameData, controllerId, permanentCards.getFirst(),
                        entry.getCard().getName(), null, false, false, false,
                        winterEffect.battlefieldEntryReplacement());
                return;
            }

            gameData.queueInteraction(new PendingReturnExiledWithSourceCard(true, controllerId,
                    winterEffect.battlefieldEntryReplacement()));
            List<UUID> validIds = permanentCards.stream().map(Card::getId).toList();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                    controllerId, new ArrayList<>(permanentCards), validIds,
                    false, false, false, false, false, 0, null, 1,
                    "Choose a permanent card to put onto the battlefield.", false, 1,
                    false, null, false));
            mutationCoordinator.invalidateAllPlayerViews(gameData);
            return;
        }

        List<Card> candidates = matchingGraveyardCards(gameData, entry);
        if (candidates.isEmpty()) {
            return;
        }
        beginChoice(gameData, entry, candidates);
    }

    private void beginChoice(GameData gameData, StackEntry entry, List<Card> candidates) {
        gameData.graveyardTargetOperation.resolutionTimeExileAnyNumberWithFourCardTypesResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(
                gameData, entry.getControllerId(), new ArrayList<>(candidates), candidates.size(), 0,
                entry.getCard().getName() + " — You may exile any number of cards from your graveyard.");
    }

    private List<Card> matchingGraveyardCards(GameData gameData, StackEntry entry) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return List.of();
        }
        UUID sourceCardId = entry.getCard().getId();
        return graveyard.stream()
                .filter(card -> !card.getId().equals(sourceCardId))
                .toList();
    }

    private List<Card> cardsStillInGraveyard(GameData gameData, UUID controllerId, List<UUID> cardIds) {
        if (cardIds == null || cardIds.isEmpty()) {
            return List.of();
        }
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return List.of();
        }
        Set<UUID> chosenIds = Set.copyOf(cardIds);
        return graveyard.stream().filter(card -> chosenIds.contains(card.getId())).toList();
    }

    private Set<CardType> cardTypes(List<Card> cards) {
        EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
        for (Card card : cards) {
            for (CardType type : CardType.values()) {
                if (card.hasType(type)) {
                    types.add(type);
                }
            }
        }
        return types;
    }

    private boolean isPermanentCard(Card card) {
        return (card.getType() != null && card.getType().isPermanentType())
                || card.getAdditionalTypes().stream().anyMatch(CardType::isPermanentType);
    }
}
