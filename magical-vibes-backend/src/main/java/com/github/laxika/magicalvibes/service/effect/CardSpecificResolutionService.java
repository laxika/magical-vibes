package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.effect.w.WarpWorldEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.WarpWorldAuraChoiceRequest;
import com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GenesisWaveEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KothEmblemEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardSpecificResolutionService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;

    @HandlesEffect(WarpWorldEffect.class)
    void resolveWarpWorld(GameData gameData, StackEntry entry) {
        Map<UUID, Integer> permanentsOwnedByPlayer = new HashMap<>();
        Map<UUID, List<Card>> permanentsToShuffleByOwner = new HashMap<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            permanentsOwnedByPlayer.put(playerId, 0);
            permanentsToShuffleByOwner.put(playerId, new ArrayList<>());
        }

        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) {
                continue;
            }

            Iterator<Permanent> iterator = battlefield.iterator();
            while (iterator.hasNext()) {
                Permanent permanent = iterator.next();
                UUID ownerId = gameData.stolenCreatures.getOrDefault(permanent.getId(), controllerId);
                if (!permanentsOwnedByPlayer.containsKey(ownerId)) {
                    continue;
                }

                permanentsOwnedByPlayer.merge(ownerId, 1, Integer::sum);
                if (!permanent.getOriginalCard().isToken()) {
                    permanentsToShuffleByOwner.get(ownerId).add(permanent.getOriginalCard());
                }
                gameData.stolenCreatures.remove(permanent.getId());
                gameData.untilEndOfTurnStolenCreatures.remove(permanent.getId());
                gameData.enchantmentDependentStolenCreatures.remove(permanent.getId());
                gameData.permanentControlStolenCreatures.remove(permanent.getId());
                iterator.remove();
            }
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> ownedPermanents = permanentsToShuffleByOwner.get(playerId);
            if (ownedPermanents.isEmpty()) {
                continue;
            }

            List<Card> deck = gameData.playerDecks.get(playerId);
            deck.addAll(ownedPermanents);
            Collections.shuffle(deck);
        }

        Map<UUID, List<Card>> revealedByPlayer = new HashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            int ownedPermanentCount = permanentsOwnedByPlayer.getOrDefault(playerId, 0);
            List<Card> deck = gameData.playerDecks.get(playerId);
            int revealCount = Math.min(ownedPermanentCount, deck.size());

            List<Card> revealed = new ArrayList<>();
            for (int i = 0; i < revealCount; i++) {
                revealed.add(deck.removeFirst());
            }
            revealedByPlayer.put(playerId, revealed);
        }

        Map<UUID, List<Card>> putOntoBattlefieldByPlayer = new HashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            putOntoBattlefieldByPlayer.put(playerId, new ArrayList<>());
        }

        // Snapshot replacement effects from permanents that existed before this Warp World event.
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(gameHelper.snapshotEnterTappedTypes(gameData));

        // First, put artifact/creature/land cards onto the battlefield.
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> revealed = revealedByPlayer.getOrDefault(playerId, List.of());
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);

            for (Card card : revealed) {
                CardType type = card.getType();
                if (type == CardType.ARTIFACT || type == CardType.CREATURE || type == CardType.LAND) {
                    Permanent permanent = new Permanent(card);
                    gameHelper.putPermanentOntoBattlefield(gameData, playerId, permanent, enterTappedTypesSnapshot);
                    putOntoBattlefieldByPlayer.get(playerId).add(card);
                }
            }
        }

        List<UUID> auraLegalBaseTargetIds = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                auraLegalBaseTargetIds.addAll(battlefield.stream().map(Permanent::getId).toList())
        );

        List<UUID> choiceOrder = getApnapOrder(gameData);

        // Then, put enchantment cards onto the battlefield.
        gameData.warpWorldOperation.pendingAuraChoices.clear();
        gameData.warpWorldOperation.pendingEnchantmentPlacements.clear();
        for (UUID playerId : choiceOrder) {
            List<Card> revealed = revealedByPlayer.getOrDefault(playerId, List.of());

            for (Card card : revealed) {
                if (card.getType() == CardType.ENCHANTMENT) {
                    if (card.isAura()) {
                        List<UUID> validTargets = findLegalAuraAttachments(gameData, card, playerId, auraLegalBaseTargetIds);
                        if (validTargets.size() == 1) {
                            UUID attachmentTargetId = validTargets.getFirst();
                            gameData.warpWorldOperation.pendingEnchantmentPlacements.add(
                                    new WarpWorldEnchantmentPlacement(playerId, card, attachmentTargetId)
                            );
                            putOntoBattlefieldByPlayer.get(playerId).add(card);
                        } else if (!validTargets.isEmpty()) {
                            gameData.warpWorldOperation.pendingAuraChoices.addLast(
                                    new WarpWorldAuraChoiceRequest(playerId, card, validTargets)
                            );
                            // Will be put onto battlefield after choosing what it enchants.
                            putOntoBattlefieldByPlayer.get(playerId).add(card);
                        }
                    } else {
                        gameData.warpWorldOperation.pendingEnchantmentPlacements.add(
                                new WarpWorldEnchantmentPlacement(playerId, card, null)
                        );
                        putOntoBattlefieldByPlayer.get(playerId).add(card);
                    }
                }
            }
        }

        // Put the rest on the bottom of each player's library (in chosen order).
        gameData.pendingLibraryBottomReorders.clear();
        for (UUID playerId : choiceOrder) {
            List<Card> revealed = revealedByPlayer.getOrDefault(playerId, List.of());
            List<Card> deck = gameData.playerDecks.get(playerId);
            List<Card> putCards = putOntoBattlefieldByPlayer.get(playerId);
            List<Card> remaining = new ArrayList<>();

            for (Card card : revealed) {
                if (!putCards.contains(card)) {
                    remaining.add(card);
                }
            }

            if (remaining.size() <= 1) {
                deck.addAll(remaining);
            } else {
                gameData.pendingLibraryBottomReorders.addLast(new LibraryBottomReorderRequest(playerId, remaining));
            }
        }

        // Save post-resolution work until bottom-order choices are complete.
        gameData.warpWorldOperation.pendingCreaturesByPlayer.clear();
        gameData.warpWorldOperation.enterTappedTypesSnapshot.clear();
        gameData.warpWorldOperation.enterTappedTypesSnapshot.addAll(enterTappedTypesSnapshot);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> creatures = putOntoBattlefieldByPlayer.get(playerId).stream()
                    .filter(card -> card.getType() == CardType.CREATURE)
                    .toList();
            gameData.warpWorldOperation.pendingCreaturesByPlayer.put(playerId, new ArrayList<>(creatures));
        }
        gameData.warpWorldOperation.needsLegendChecks = true;
        gameData.warpWorldOperation.sourceName = entry.getCard().getName();

        if (!gameData.warpWorldOperation.pendingAuraChoices.isEmpty()) {
            gameHelper.beginNextPendingWarpWorldAuraChoice(gameData);
            return;
        }
        gameHelper.placePendingWarpWorldEnchantments(gameData);
        if (!gameData.pendingLibraryBottomReorders.isEmpty()) {
            gameHelper.beginNextPendingLibraryBottomReorder(gameData);
            return;
        }

        gameHelper.finalizePendingWarpWorld(gameData);
    }

    @HandlesEffect(GenesisWaveEffect.class)
    void resolveGenesisWave(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        int xValue = entry.getXValue();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (xValue <= 0 || deck.isEmpty()) {
            String logMsg = playerName + " casts Genesis Wave with X=" + xValue + " — no cards revealed.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        int count = Math.min(xValue, deck.size());
        List<Card> revealedCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : revealedCards) {
            boolean isPermanent = card.getType() != CardType.INSTANT
                    && card.getType() != CardType.SORCERY;
            if (isPermanent && card.getManaValue() <= xValue) {
                eligibleCards.add(card);
            }
        }

        String logMsg = playerName + " reveals the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);

        if (eligibleCards.isEmpty()) {
            for (Card card : revealedCards) {
                gameHelper.addCardToGraveyard(gameData, controllerId, card);
            }
            String graveyardLog = playerName + " finds no eligible permanent cards. All revealed cards are put into their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, graveyardLog);
            return;
        }

        Set<UUID> validCardIds = ConcurrentHashMap.newKeySet();
        for (Card card : eligibleCards) {
            validCardIds.add(card.getId());
        }

        gameData.interaction.beginLibraryRevealChoice(controllerId, revealedCards, validCardIds, true);

        List<CardView> cardViews = eligibleCards.stream().map(cardViewFactory::create).toList();
        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseMultipleCardsFromGraveyardsMessage(
                cardIds, cardViews, eligibleCards.size(),
                "Choose any number of permanent cards with mana value " + xValue + " or less to put onto the battlefield."
        ));

        log.info("Game {} - {} resolving Genesis Wave with X={}, {} revealed, {} eligible",
                gameData.id, playerName, xValue, count, eligibleCards.size());
    }

    @HandlesEffect(KothEmblemEffect.class)
    void resolveKothEmblem(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        ActivatedAbility mountainAbility = new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: This land deals 1 damage to any target."
        );

        Emblem emblem = new Emblem(controllerId, List.of(
                new GrantActivatedAbilityEffect(mountainAbility, GrantScope.OWN_PERMANENTS,
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN))
        ));

        gameData.emblems.add(emblem);

        String logEntry = playerName + " gets an emblem with \"Mountains you control have '{T}: This land deals 1 damage to any target.'\".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets Koth emblem", gameData.id, playerName);
    }

    private List<UUID> findLegalAuraAttachments(GameData gameData, Card auraCard, UUID auraControllerId, List<UUID> baseTargetIds) {
        List<UUID> validTargets = new ArrayList<>();
        gameData.forEachPermanent((playerId, candidate) -> {
            if (!baseTargetIds.contains(candidate.getId())) {
                return;
            }
            if (gameQueryService.hasProtectionFrom(gameData, candidate, auraCard.getColor())) {
                return;
            }
            if (auraCard.getTargetFilter() != null) {
                try {
                    gameQueryService.validateTargetFilter(auraCard.getTargetFilter(),
                            candidate,
                            FilterContext.of(gameData)
                                    .withSourceCardId(auraCard.getId())
                                    .withSourceControllerId(auraControllerId));
                } catch (IllegalStateException ignored) {
                    return;
                }
            }
            validTargets.add(candidate.getId());
        });

        return validTargets;
    }

    private List<UUID> getApnapOrder(GameData gameData) {
        if (gameData.activePlayerId == null || !gameData.orderedPlayerIds.contains(gameData.activePlayerId)) {
            return new ArrayList<>(gameData.orderedPlayerIds);
        }

        int activeIndex = gameData.orderedPlayerIds.indexOf(gameData.activePlayerId);
        List<UUID> apnap = new ArrayList<>(gameData.orderedPlayerIds.size());
        for (int i = 0; i < gameData.orderedPlayerIds.size(); i++) {
            int idx = (activeIndex + i) % gameData.orderedPlayerIds.size();
            apnap.add(gameData.orderedPlayerIds.get(idx));
        }
        return apnap;
    }
}

