package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeGrantingPermanentAndControllerDrawsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SacrificeGrantingPermanentAndControllerDrawsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeGrantingPermanentAndControllerDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeGrantingPermanentAndControllerDrawsEffect) effect;
        UUID grantingPermanentId = e.grantingPermanentId();
        if (grantingPermanentId == null) {
            return;
        }

        Permanent grantingPermanent = gameQueryService.findPermanentById(gameData, grantingPermanentId);
        if (grantingPermanent == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, grantingPermanentId);
        if (!permanentRemovalService.removePermanentToGraveyard(gameData, grantingPermanent)) {
            return;
        }

        triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                gameData, controllerId, grantingPermanent.getCard());
        gameLogService.append(gameData, GameLog.cardThen(grantingPermanent.getCard(), " is sacrificed."));
        permanentRemovalService.removeOrphanedAuras(gameData);
        playerInteractionSupport.applyDrawCards(gameData, controllerId, e.cards());
    }
}
