package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenDrawAndLoseLifePerCounterEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCounterOnSelfThenDrawAndLoseLifePerCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnSelfThenDrawAndLoseLifePerCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnSelfThenDrawAndLoseLifePerCounterEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        Permanent counted = self != null ? self : entry.getSourcePermanentSnapshot();
        if (counted == null) {
            return;
        }

        if (self != null) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, self, e.counterType(), 1);
        }
        int amount = amountEvaluationService.evaluate(gameData, e.drawnCardAmount(),
                AmountContext.forStackEntry(entry, counted));
        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), amount);
        lifeSupport.applyLifeLoss(gameData, entry.getControllerId(), amount, entry.getCard().getName());
    }
}
