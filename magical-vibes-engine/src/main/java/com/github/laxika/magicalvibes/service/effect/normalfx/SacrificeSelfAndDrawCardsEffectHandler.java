package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDrawCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SacrificeSelfAndDrawCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfAndDrawCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfAndDrawCardsEffect) effect;

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles — source no longer on the battlefield."));
            return;
        }

        // "Sacrifice it. If you do, draw ..." — the draw is contingent on the sacrifice happening,
        // and the sacrifice must fire ally-sacrifice triggers and clean up orphaned Auras exactly
        // as SacrificeSelfEffectHandler does.
        if (!permanentRemovalService.removePermanentToGraveyard(gameData, source)) {
            return;
        }

        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, entry.getControllerId(), source.getCard());
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is sacrificed."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), e.amount());
    }
}
