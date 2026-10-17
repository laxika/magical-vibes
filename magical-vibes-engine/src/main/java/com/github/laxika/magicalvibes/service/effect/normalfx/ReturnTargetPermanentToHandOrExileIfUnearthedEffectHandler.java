package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandOrExileIfUnearthedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetPermanentToHandOrExileIfUnearthedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentToHandOrExileIfUnearthedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID ownerId = target.getCard().getOwnerId() != null
                ? target.getCard().getOwnerId()
                : gameQueryService.findPermanentController(gameData, target.getId());
        boolean hasUnearth = gameQueryService.cardHasUnearthAbility(gameData, ownerId, target.getCard());
        boolean removed = hasUnearth
                ? permanentRemovalService.removeUnearthedPermanentToHand(gameData, target)
                : permanentRemovalService.removePermanentToHand(gameData, target);
        if (removed) {
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                    " is returned to its owner's hand."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
