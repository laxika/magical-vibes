package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedExileReturnCounterTrigger;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesUntilSourceLeavesWithCounterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves targeted battlefield exile that returns cards when the source leaves. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreaturesUntilSourceLeavesWithCounterEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreaturesUntilSourceLeavesWithCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTargetCreaturesUntilSourceLeavesWithCounterEffect) effect;
        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry);
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                continue;
            }

            Card card = target.getOriginalCard();
            UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), targetControllerId);
            boolean token = target.getCard().isToken();

            if (!permanentRemovalService.removePermanentToExile(gameData, target, sourcePermanentId)) {
                continue;
            }
            gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled by ", entry.getCard(), "."));
            log.info("Game {} - {} exiles {} until it leaves the battlefield",
                    gameData.id, entry.getCard().getName(), card.getName());

            if (sourcePermanentId != null && !token) {
                gameData.addExileReturnOnPermanentLeave(sourcePermanentId,
                        new PendingExileReturn(card, ownerId));
            }
        }

        if (sourcePermanentId != null) {
            gameData.queueDelayedAction(new DelayedExileReturnCounterTrigger(
                    sourcePermanentId,
                    entry.getControllerId(),
                    entry.getCard(),
                    exileEffect.counterType(),
                    exileEffect.counterAmount()));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private UUID resolveSourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null
                && gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) != null) {
            return entry.getSourcePermanentId();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getCard() == entry.getCard()) {
                return permanent.getId();
            }
        }
        return null;
    }
}
