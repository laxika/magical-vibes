package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilOpponentBecomesMonarchEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentUntilOpponentBecomesMonarchEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentUntilOpponentBecomesMonarchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            Card card = target.getOriginalCard();
            UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), targetControllerId);
            if (!permanentRemovalService.removePermanentToExile(gameData, target)) {
                continue;
            }

            gameData.addExileReturnOnOpponentBecomesMonarch(
                    controllerId, new PendingExileReturn(card, ownerId));
            gameLogService.append(gameData, GameLog.cardTextCard(
                    card, " is exiled by ", entry.getCard(), " until an opponent becomes the monarch."));
            log.info("Game {} - {} exiles {} until an opponent becomes the monarch",
                    gameData.id, entry.getCard().getName(), card.getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
