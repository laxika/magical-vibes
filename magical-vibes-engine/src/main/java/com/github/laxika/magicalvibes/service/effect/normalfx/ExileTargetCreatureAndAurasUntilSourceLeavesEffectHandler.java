package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedSourceUntapsOrLeavesTrigger;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndAurasUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ResolvePendingExileReturnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the source-linked exile used by Tawnos's Coffin, and creates its delayed "when this artifact leaves the
 * battlefield or becomes untapped, return that exiled card" ability: one {@link DelayedSourceUntapsOrLeavesTrigger}
 * for this resolution's creature and Auras.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureAndAurasUntilSourceLeavesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureAndAurasUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent target = entry.getTargetId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        boolean targetIsToken = target.getCard().isToken();
        Map<CounterType, Integer> counters = new EnumMap<>(target.getCounters());
        List<Card> targetCards = target.cardsLeavingBattlefield();
        if (targetCards.isEmpty()) {
            return;
        }

        List<Card> additionalCards = new ArrayList<>(targetCards.subList(1, targetCards.size()));
        Set<UUID> cardsToAttach = new LinkedHashSet<>();
        List<Permanent> attachedAuras = attachedAuras(gameData, target.getId());
        for (Permanent aura : attachedAuras) {
            List<Card> auraCards = aura.cardsLeavingBattlefield();
            if (auraCards.isEmpty()) {
                continue;
            }
            UUID trackingSourceId = targetIsToken ? null : sourcePermanentId;
            if (!permanentRemovalService.removePermanentToExile(gameData, aura, trackingSourceId)) {
                continue;
            }
            additionalCards.addAll(auraCards);
            if (!targetIsToken) {
                auraCards.stream().map(Card::getId).forEach(cardsToAttach::add);
            }
        }

        if (!permanentRemovalService.removePermanentToExile(
                gameData, target, targetIsToken ? null : sourcePermanentId)) {
            return;
        }

        // "When this artifact leaves the battlefield or becomes untapped, return that exiled card": a delayed
        // triggered ability of this resolution (CR 603.7a), controlled by the ability's controller (CR 603.7e). A
        // source that left before the ability resolved can no longer leave or untap, so none is created.
        if (!targetIsToken && sourcePermanentId != null
                && gameQueryService.findPermanentById(gameData, sourcePermanentId) != null) {
            Card primary = targetCards.getFirst();
            UUID ownerId = ownerOf(gameData, primary, target.getCard().getOwnerId());
            // "Under their owner's control": each other exiled card returns under its own owner's control.
            Map<UUID, UUID> owners = new HashMap<>();
            additionalCards.forEach(card -> owners.put(card.getId(), ownerOf(gameData, card, ownerId)));
            PendingExileReturn pending = PendingExileReturn.withCountersAndCardsAttachedToPrimary(
                    primary, ownerId, true, additionalCards, cardsToAttach, counters)
                    .withReturnControllers(owners);
            gameData.queueDelayedAction(new DelayedSourceUntapsOrLeavesTrigger(entry.getControllerId(),
                    entry.getCard(), sourcePermanentId, null, new ResolvePendingExileReturnEffect(pending,
                    gameData.exileEntryVersions.getOrDefault(primary.getId(), 0L))));
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData,
                GameLog.cardTextCard(targetCards.getFirst(), " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles {} and its attached Auras",
                gameData.id, entry.getCard().getName(), targetCards.getFirst().getName());
    }

    private List<Permanent> attachedAuras(GameData gameData, UUID targetId) {
        List<Permanent> result = new ArrayList<>();
        gameData.forEachPermanent((ignored, permanent) -> {
            if (targetId.equals(permanent.getAttachedTo()) && permanent.getCard().isAura()) {
                result.add(permanent);
            }
        });
        return result;
    }

    private UUID ownerOf(GameData gameData, Card card, UUID fallback) {
        var exiledEntry = gameData.findExiledCard(card.getId());
        return exiledEntry == null ? fallback : exiledEntry.ownerId();
    }
}
