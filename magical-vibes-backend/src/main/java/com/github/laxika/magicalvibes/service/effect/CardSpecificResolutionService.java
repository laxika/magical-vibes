package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.effect.w.WarpWorldEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.WarpWorldAuraChoiceRequest;
import com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardSpecificResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(WarpWorldEffect.class,
                (gd, entry, effect) -> resolveWarpWorld(gd, entry));
    }

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

        // First, put artifact/creature/land cards onto the battlefield.
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> revealed = revealedByPlayer.getOrDefault(playerId, List.of());
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);

            for (Card card : revealed) {
                CardType type = card.getType();
                if (type == CardType.ARTIFACT || type == CardType.CREATURE || type == CardType.LAND) {
                    Permanent permanent = new Permanent(card);
                    battlefield.add(permanent);
                    putOntoBattlefieldByPlayer.get(playerId).add(card);
                }
            }
        }

        List<UUID> auraLegalBaseTargetIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                auraLegalBaseTargetIds.addAll(battlefield.stream().map(Permanent::getId).toList());
            }
        }

        List<UUID> choiceOrder = getApnapOrder(gameData);

        // Then, put enchantment cards onto the battlefield.
        gameData.warpWorldOperation.pendingAuraChoices.clear();
        gameData.warpWorldOperation.pendingEnchantmentPlacements.clear();
        for (UUID playerId : choiceOrder) {
            List<Card> revealed = revealedByPlayer.getOrDefault(playerId, List.of());

            for (Card card : revealed) {
                if (card.getType() == CardType.ENCHANTMENT) {
                    if (card.isAura()) {
                        List<UUID> validTargets = findLegalAuraAttachments(gameData, card, auraLegalBaseTargetIds);
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

    private List<UUID> findLegalAuraAttachments(GameData gameData, Card auraCard, List<UUID> baseTargetIds) {
        List<UUID> validTargets = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }

            for (Permanent candidate : battlefield) {
                if (!baseTargetIds.contains(candidate.getId())) {
                    continue;
                }
                if (gameQueryService.hasProtectionFrom(gameData, candidate, auraCard.getColor())) {
                    continue;
                }
                if (auraCard.getTargetFilter() != null) {
                    try {
                        gameQueryService.validateTargetFilter(gameData, auraCard.getTargetFilter(), candidate);
                    } catch (IllegalStateException ignored) {
                        continue;
                    }
                }
                validTargets.add(candidate.getId());
            }
        }

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

