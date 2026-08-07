package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndControllerDrawsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Sacrifices the source permanent on behalf of <em>its</em> controller and makes that same player
 * draw. The stack entry's controller is the activating opponent (Soul Ransom's ability is only
 * activatable by opponents), so neither half may use {@code entry.getControllerId()}.
 */
@Component
@RequiredArgsConstructor
public class SacrificeSelfAndControllerDrawsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfAndControllerDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfAndControllerDrawsEffect) effect;

        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles — source no longer on the battlefield."));
            return;
        }

        UUID sourceController = gameQueryService.findPermanentController(gameData, self.getId());
        if (!permanentRemovalService.removePermanentToGraveyard(gameData, self)) {
            return;
        }

        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, sourceController, self.getCard());
        gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " is sacrificed."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        playerInteractionSupport.applyDrawCards(gameData, sourceController, e.cards());
    }
}
