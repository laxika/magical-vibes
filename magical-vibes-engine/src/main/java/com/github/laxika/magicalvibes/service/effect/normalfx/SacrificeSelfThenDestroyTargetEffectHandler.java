package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenDestroyTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Sacrifices the source permanent, then — only if the sacrifice succeeded ("if you do") — destroys
 * the stack entry's target permanent. Wasp of the Bitter End's spell-cast trigger.
 */
@Component
@RequiredArgsConstructor
public class SacrificeSelfThenDestroyTargetEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfThenDestroyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null || !permanentRemovalService.removePermanentToGraveyard(gameData, self)) {
            // "If you do" — no sacrifice, no destroy.
            return;
        }
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, entry.getControllerId(), self.getCard());
        gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " is sacrificed."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target != null) {
            destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), false);
        }
    }
}
