package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureWithSuspendEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Suspend's exile-and-suspend effect. */
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureWithSuspendEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureWithSuspendEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTargetCreatureWithSuspendEffect suspendEffect =
                (ExileTargetCreatureWithSuspendEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                continue;
            }

            List<Card> leavingCards = target.cardsLeavingBattlefield();
            if (!permanentRemovalService.removePermanentToExile(gameData, target)) {
                continue;
            }

            for (Card card : leavingCards) {
                if (card.isToken() || gameData.findExiledCard(card.getId()) == null) {
                    continue;
                }
                gameData.exiledCardTimeCounters.put(card.getId(), suspendEffect.timeCounters());
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " is exiled with " + suspendEffect.timeCounters()
                                + " time counters and gains suspend."));
            }
        }
    }
}
