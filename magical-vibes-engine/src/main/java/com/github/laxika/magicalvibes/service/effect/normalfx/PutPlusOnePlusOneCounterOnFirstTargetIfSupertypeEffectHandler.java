package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnFirstTargetIfSupertypeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutPlusOnePlusOneCounterOnFirstTargetIfSupertypeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutPlusOnePlusOneCounterOnFirstTargetIfSupertypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutPlusOnePlusOneCounterOnFirstTargetIfSupertypeEffect) effect;
        if (entry.getTargetIds() == null || entry.getTargetIds().isEmpty()) {
            return;
        }

        UUID firstTargetId = entry.getTargetIds().getFirst();
        Permanent firstTarget = gameQueryService.findPermanentById(gameData, firstTargetId);
        if (firstTarget == null) {
            return;
        }

        if (!firstTarget.getCard().getSupertypes().contains(e.supertype())) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, firstTarget)) {
            return;
        }

        permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, firstTarget, e.count());
    }
}
