package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndAurasUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the source-linked exile used by Tawnos's Coffin. */
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

        if (!targetIsToken && sourcePermanentId != null) {
            UUID ownerId = ownerOf(gameData, targetCards.getFirst(), target.getCard().getOwnerId());
            gameData.addExileReturnOnPermanentLeave(sourcePermanentId,
                    PendingExileReturn.withCountersAndCardsAttachedToPrimary(
                            targetCards.getFirst(), ownerId, true, additionalCards,
                            cardsToAttach, counters));
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
