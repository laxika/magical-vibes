package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a targeted creature bounce with a perpetual generic cast-cost change. */
@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(e);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            Card card = target.getCard();
            if (!permanentRemovalService.removePermanentToHand(gameData, target)) {
                continue;
            }
            if (e.increase()) {
                PerpetualCardCastCostSupport.rememberIncrease(gameData, card, e.amount());
            } else {
                PerpetualCardCastCostSupport.remember(gameData, card, e.amount());
            }
            String direction = e.increase() ? "more" : "less";
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is returned to its owner's hand and perpetually costs "
                            + e.amount() + " generic mana " + direction + " to cast."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
