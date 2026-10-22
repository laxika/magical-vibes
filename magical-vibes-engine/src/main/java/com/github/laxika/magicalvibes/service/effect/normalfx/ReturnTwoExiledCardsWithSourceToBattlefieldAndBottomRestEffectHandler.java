package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    private final ReturnAllCardsExiledWithSourceEffectHandler returnAllHandler;
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
        Map<UUID, List<Card>> cardsPutIntoLibraries = new LinkedHashMap<>();

        ExiledCardEntry auraAwaitingHost = null;
        List<UUID> auraHostCandidates = List.of();
        for (ExiledCardEntry entry : entries) {
            if (!selectedIds.contains(entry.card().getId()) || !isNonCreatureAura(entry.card())) {
                continue;
            }
            List<UUID> hosts = returnAllHandler.legalAuraHosts(gameData, entry.card(), entry.ownerId());
            if (auraAwaitingHost == null && !hosts.isEmpty()) {
                auraAwaitingHost = entry;
                auraHostCandidates = hosts;
            }
        }

        for (ExiledCardEntry entry : entries) {
            Card card = entry.card();
            if (selectedIds.contains(card.getId())) {
                if (isNonCreatureAura(card)) {
                    continue;
                }
                returnCardHandler.returnToBattlefield(gameData, entry.ownerId(), card, sourceName,
                        null, false, false, false, enterTappedTypes, simultaneouslyEntered);
            } else if (gameData.removeFromExile(card.getId())) {
                cardsPutIntoLibraries.computeIfAbsent(entry.ownerId(), ignored -> new ArrayList<>()).add(card);
            }
        }

        boolean awaitingAuraHost = auraAwaitingHost != null;
        if (awaitingAuraHost) {
            returnAllHandler.beginAuraReturnChoice(gameData, auraAwaitingHost.ownerId(), auraAwaitingHost,
                    auraHostCandidates);
        }

        boolean reorderPrompted = awaitingAuraHost;
        for (Map.Entry<UUID, List<Card>> group : cardsPutIntoLibraries.entrySet()) {
            UUID ownerId = group.getKey();
            List<Card> cards = group.getValue();
            if (cards.size() == 1) {
                gameData.playerDecks.get(ownerId).addLast(cards.getFirst());
            } else if (!reorderPrompted) {
                reorderPrompted = true;
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                        ownerId, new ArrayList<>(cards), true, ownerId,
                        "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
            } else {
                gameData.pendingLibraryBottomReorders.addLast(
                        new LibraryBottomReorderRequest(ownerId, new ArrayList<>(cards)));
            }
            triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, ownerId, cards.size());
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(ownerId) + " puts " + cards.size()
                            + " card(s) on the bottom of their library."));
        }
    }

    private static boolean isNonCreatureAura(Card card) {
        return card.isAura() && !card.hasType(CardType.CREATURE);
    }
}
