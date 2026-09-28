package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingReturnTwoExiledWithSourceCards;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTwoExiledCardsWithSourceToBattlefieldAndBottomRestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Vault 13's Chapter III source-linked exile choice. */
@Component
@RequiredArgsConstructor
public class ReturnTwoExiledCardsWithSourceToBattlefieldAndBottomRestEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameMutationCoordinator mutationCoordinator;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final ReturnCardExiledWithSourceToBattlefieldEffectHandler returnCardHandler;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTwoExiledCardsWithSourceToBattlefieldAndBottomRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        List<ExiledCardEntry> matching = matchingEntries(gameData, sourcePermanentId);
        if (matching.size() <= 2) {
            resolveCards(gameData, matching, matching.stream().map(e -> e.card().getId()).toList(),
                    entry.getCard().getName());
            return;
        }

        UUID controllerId = entry.getControllerId();
        gameData.queueInteraction(new PendingReturnTwoExiledWithSourceCards(
                controllerId, sourcePermanentId));
        List<Card> cards = matching.stream().map(ExiledCardEntry::card).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, new ArrayList<>(cards), cards.stream().map(Card::getId).toList(),
                false, false, false, false, false, 0, null, 2,
                "Choose two cards exiled with " + entry.getCard().getName()
                        + " to return to the battlefield.",
                2, false));
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    public void completeChoice(GameData gameData, PendingReturnTwoExiledWithSourceCards pending,
                               List<Card> revealedCards, List<UUID> selectedCardIds) {
        List<ExiledCardEntry> matching = matchingEntries(gameData, pending.sourcePermanentId());
        Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        String sourceName = source == null ? "the Saga" : source.getCard().getName();
        resolveCards(gameData, matching, selectedCardIds, sourceName);
    }

    private List<ExiledCardEntry> matchingEntries(GameData gameData, UUID sourcePermanentId) {
        return gameData.exiledCards.stream()
                .filter(entry -> sourcePermanentId.equals(entry.sourcePermanentId()))
                .toList();
    }

    private void resolveCards(GameData gameData, List<ExiledCardEntry> entries,
                              List<UUID> selectedCardIds, String sourceName) {
        Set<UUID> selectedIds = new HashSet<>(selectedCardIds);
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        Map<UUID, Integer> cardsPutIntoLibraries = new HashMap<>();

        for (ExiledCardEntry entry : entries) {
            Card card = entry.card();
            if (selectedIds.contains(card.getId())) {
                returnCardHandler.returnToBattlefield(gameData, entry.ownerId(), card, sourceName,
                        null, false, false, false, enterTappedTypes, simultaneouslyEntered);
            } else if (gameData.removeFromExile(card.getId())) {
                gameData.playerDecks.get(entry.ownerId()).addLast(card);
                cardsPutIntoLibraries.merge(entry.ownerId(), 1, Integer::sum);
            }
        }

        cardsPutIntoLibraries.forEach((ownerId, count) -> {
            triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, ownerId, count);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(ownerId) + " puts " + count
                            + " card(s) on the bottom of their library."));
        });
    }
}
