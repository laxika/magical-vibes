package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenDealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Sacrifices the source permanent, then — only if the sacrifice succeeded ("if you do") — deals the
 * effect's damage to the stack entry's target player. Booby Trap's draw trigger.
 */
@Component
@RequiredArgsConstructor
public class SacrificeSelfThenDealDamageToTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final DamageSupport damageSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfThenDealDamageToTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfThenDealDamageToTargetPlayerEffect) effect;

        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null || !permanentRemovalService.removePermanentToGraveyard(gameData, self)) {
            // "If you do" — no sacrifice, no damage.
            return;
        }
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, entry.getControllerId(), self.getCard());
        gameBroadcastService.logAndBroadcast(gameData, self.getCard().getName() + " is sacrificed.");
        permanentRemovalService.removeOrphanedAuras(gameData);

        UUID targetId = entry.getTargetId();
        if (gameData.playerIds.contains(targetId)
                && !damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
            gameOutcomeService.checkWinCondition(gameData);
        }
    }
}
