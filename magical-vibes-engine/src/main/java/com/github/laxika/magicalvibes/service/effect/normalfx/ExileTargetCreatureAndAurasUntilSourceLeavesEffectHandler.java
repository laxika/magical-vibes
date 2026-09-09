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
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry);
        boolean sourceOnBattlefield = sourcePermanentId != null
                && gameQueryService.findPermanentById(gameData, sourcePermanentId) != null;
        UUID exileSourcePermanentId = sourceOnBattlefield ? sourcePermanentId : null;
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), targetControllerId);
        List<Card> creatureCards = new ArrayList<>(target.cardsLeavingBattlefield());
        if (creatureCards.isEmpty()) {
            return;
        }

        Map<CounterType, Integer> counters = snapshotCounters(target);
        List<Permanent> attachedAuras = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (target.getId().equals(permanent.getAttachedTo()) && permanent.getCard().isAura()) {
                attachedAuras.add(permanent);
            }
        });

        List<Card> auraCards = new ArrayList<>();
        Set<UUID> cardsToAttach = new LinkedHashSet<>();
        for (Permanent aura : attachedAuras) {
            List<Card> leavingCards = aura.cardsLeavingBattlefield();
            if (leavingCards.isEmpty()
                    || !permanentRemovalService.removePermanentToExile(
                    gameData, aura, exileSourcePermanentId)) {
                continue;
            }
            auraCards.addAll(leavingCards);
            leavingCards.stream().map(Card::getId).forEach(cardsToAttach::add);
        }

        if (!permanentRemovalService.removePermanentToExile(gameData, target, exileSourcePermanentId)) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            return;
        }

        Card primaryCard = creatureCards.getFirst();
        if (sourceOnBattlefield && !primaryCard.isToken()) {
            List<Card> additionalCards = new ArrayList<>(creatureCards.subList(1, creatureCards.size()));
            additionalCards.addAll(auraCards);
            gameData.addExileReturnOnPermanentLeave(sourcePermanentId,
                    PendingExileReturn.withCardsAttachedToPrimaryAndCounters(
                            primaryCard, ownerId, true, additionalCards, cardsToAttach, counters));
        }

        gameLogService.append(gameData, GameLog.cardTextCard(
                primaryCard, " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles {} and its attached Auras until it leaves or untaps",
                gameData.id, entry.getCard().getName(), primaryCard.getName());
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private UUID resolveSourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return null;
        }
        return battlefield.stream()
                .filter(permanent -> permanent.getCard() == entry.getCard())
                .map(Permanent::getId)
                .findFirst()
                .orElse(null);
    }

    private Map<CounterType, Integer> snapshotCounters(Permanent permanent) {
        Map<CounterType, Integer> counters = new EnumMap<>(CounterType.class);
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            int count = permanent.getCounterCount(counterType);
            if (count > 0) {
                counters.put(counterType, count);
            }
        }
        return counters;
    }
}
