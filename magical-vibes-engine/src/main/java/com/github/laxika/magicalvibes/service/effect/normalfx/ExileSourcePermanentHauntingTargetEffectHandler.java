package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourcePermanentHauntingTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Exiles the source card and links it to the targeted creature until that creature leaves the battlefield. */
@Component
@RequiredArgsConstructor
public class ExileSourcePermanentHauntingTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;

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
        if (target == null) {
            return;
        }

        Card sourceCard;
        UUID ownerId;
        if (source != null) {
            sourceCard = source.getCard();
            UUID controllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
            ownerId = sourceCard.getOwnerId() != null ? sourceCard.getOwnerId() : controllerId;
            if (!permanentRemovalService.removePermanentToExile(gameData, source)) {
                return;
            }
        } else {
            sourceCard = entry.getCard();
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCard.getId());
            if (graveyardOwnerId == null) {
                return;
            }
            ownerId = sourceCard.getOwnerId() != null ? sourceCard.getOwnerId() : graveyardOwnerId;
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, sourceCard.getId());
            exileService.exileCard(gameData, ownerId, sourceCard);
        }

        gameData.hauntingCardToPermanentId.put(sourceCard.getId(), target.getId());
        gameData.addExileReturnOnPermanentLeave(target.getId(), new PendingExileReturn(sourceCard, ownerId));
        gameLogService.append(gameData,
                GameLog.cardThen(sourceCard, " is exiled haunting " + target.getCard().getName() + "."));
    }

    private UUID targetId(StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.targetsForEffect(effect);
        return targets.isEmpty() ? entry.getTargetId() : targets.getFirst();
    }
}
