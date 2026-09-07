package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateXTokenWithXCountersEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateXTokenWithXCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateXTokenWithXCountersEffect) effect;
        int counterAmount = amountEvaluationService.evaluate(
                gameData, e.counterAmount(), AmountContext.forStackEntry(entry, null));
        if (counterAmount < 0) {
            return;
        }

        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.tokenTemplate(), entry.getCard().getSetCode());
        if (counterAmount == 0 || createdIds.isEmpty()) {
            return;
        }

        Permanent token = gameQueryService.findPermanentById(gameData, createdIds.getLast());
        if (token == null || gameQueryService.cantHaveCounters(gameData, token)) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, token, e.counterType(), counterAmount);
    }
}
