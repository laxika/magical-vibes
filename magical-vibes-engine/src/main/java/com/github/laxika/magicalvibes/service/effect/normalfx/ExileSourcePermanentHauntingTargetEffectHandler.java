package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourcePermanentHauntingTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Exiles Kaya and links her to the targeted creature until that creature leaves the battlefield. */
@Component
@RequiredArgsConstructor
public class ExileSourcePermanentHauntingTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourcePermanentHauntingTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        UUID targetId = targetId(entry, effect);
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (source == null || target == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
        UUID ownerId = source.getCard().getOwnerId() != null
                ? source.getCard().getOwnerId() : controllerId;
        if (!permanentRemovalService.removePermanentToExile(gameData, source)) {
            return;
        }

        gameData.hauntingCardToPermanentId.put(source.getCard().getId(), target.getId());
        gameData.addExileReturnOnPermanentLeave(target.getId(), new PendingExileReturn(source.getCard(), ownerId));
        gameLogService.append(gameData,
                GameLog.cardThen(source.getCard(), " is exiled haunting " + target.getCard().getName() + "."));
    }

    private UUID targetId(StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.targetsForEffect(effect);
        return targets.isEmpty() ? entry.getTargetId() : targets.getFirst();
    }
}
