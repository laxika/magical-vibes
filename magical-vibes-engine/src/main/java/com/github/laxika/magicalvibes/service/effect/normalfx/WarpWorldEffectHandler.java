package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.effect.w.WarpWorldEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.WarpWorldAuraChoiceRequest;
import com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WarpWorldEffectHandler implements NormalEffectHandlerBean {

    private final WarpWorldService warpWorldService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WarpWorldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, Integer> permanentsOwnedByPlayer = new HashMap<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            permanentsOwnedByPlayer.put(playerId, 0);
        }

        // Every permanent leaves the battlefield in one simultaneous event (its leave-the-battlefield
        // abilities still see it, CR 603.10a), and each one goes into its owner's library unshuffled.
        List<Permanent> departing = new ArrayList<>();
        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) {
                continue;
            }

            for (Permanent permanent : battlefield) {
                UUID ownerId = gameData.stolenCreatures.getOrDefault(permanent.getId(), controllerId);
                if (!permanentsOwnedByPlayer.containsKey(ownerId)) {
                    continue;
                }

                permanentsOwnedByPlayer.merge(ownerId, 1, Integer::sum);
                departing.add(permanent);
            }
        }
        permanentRemovalService.performSimultaneousRemovals(gameData, departing, () -> {
            for (Permanent permanent : departing) {
                permanentRemovalService.removePermanentToLibraryUnshuffled(gameData, permanent);
            }
        });

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (permanentsOwnedByPlayer.getOrDefault(playerId, 0) == 0) {
                continue;
            }
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
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
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));

        // First, put artifact/creature/land cards onto the battlefield. Every player's cards in this
        // group enter simultaneously (CR 614.12), so none of them applies its own replacement or
        // static abilities to the others — the batch spans all players, not just one battlefield.
        // The later enchantment group is a separate, subsequent event and does see this one.
        List<Permanent> permanentGroupBatch = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> revealed = revealedByPlayer.getOrDefault(playerId, List.of());

            for (Card card : revealed) {
                if (card.hasType(CardType.ARTIFACT) || card.hasType(CardType.CREATURE)
                        || card.hasType(CardType.LAND)) {
                    Permanent permanent = new Permanent(card);
                    battlefieldEntryService.putPermanentOntoBattlefield(
                            gameData, playerId, permanent, enterTappedTypesSnapshot, permanentGroupBatch);
                    permanentGroupBatch.add(permanent);
                    putOntoBattlefieldByPlayer.get(playerId).add(card);
                }
            }
        }

        // The creature group is its own event, so its enters-the-battlefield triggers are collected
        // now, before the enchantment group arrives (CR 603.6a).
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Card card : putOntoBattlefieldByPlayer.get(playerId)) {
                if (card.hasType(CardType.CREATURE)) {
                    battlefieldEntryService.processCreatureETBEffects(gameData, playerId, card, null, false);
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
                if (card.hasType(CardType.ENCHANTMENT)
                        && !card.hasType(CardType.ARTIFACT)
                        && !card.hasType(CardType.CREATURE)
                        && !card.hasType(CardType.LAND)) {
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
        gameData.warpWorldOperation.enterTappedTypesSnapshot.clear();
        gameData.warpWorldOperation.enterTappedTypesSnapshot.addAll(enterTappedTypesSnapshot);
        gameData.warpWorldOperation.needsLegendChecks = true;
        gameData.warpWorldOperation.sourceName = entry.getCard().getName();

        if (!gameData.warpWorldOperation.pendingAuraChoices.isEmpty()) {
            warpWorldService.beginNextPendingWarpWorldAuraChoice(gameData);
            return;
        }
        warpWorldService.placePendingWarpWorldEnchantments(gameData);
        if (!gameData.pendingLibraryBottomReorders.isEmpty()) {
            warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
            return;
        }

        warpWorldService.finalizePendingWarpWorld(gameData);
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
            if (gameQueryService.hasProtectionFromSourceCardTypes(gameData, candidate, auraCard)) {
                return;
            }
            if (gameQueryService.hasProtectionFromSourceSubtypes(gameData, candidate, auraCard)) {
                return;
            }
            if (auraCard.getTargetFilter() != null) {
                try {
                    predicateEvaluationService.validateTargetFilter(auraCard.getTargetFilter(),
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
        if (auraCard.isEnchantPlayer()) {
            for (UUID playerId : gameData.playerIds) {
                if (auraAttachmentService.canEnchantPlayer(gameData, auraCard, auraControllerId, playerId)) {
                    validTargets.add(playerId);
                }
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
