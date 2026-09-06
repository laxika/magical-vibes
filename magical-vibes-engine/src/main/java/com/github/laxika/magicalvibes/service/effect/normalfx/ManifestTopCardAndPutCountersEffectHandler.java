package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardAndPutCountersEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a manifest followed by counter placement on the manifested permanent. */
@Component
@RequiredArgsConstructor
public class ManifestTopCardAndPutCountersEffectHandler implements NormalEffectHandlerBean {

    private final ManifestService manifestService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManifestTopCardAndPutCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ManifestTopCardAndPutCountersEffect e = (ManifestTopCardAndPutCountersEffect) effect;
        Permanent manifested = manifestService.manifestTopCard(gameData, entry.getControllerId(), entry.getCard());
        if (manifested != null) {
            int count = amountEvaluationService.evaluate(
                    gameData, e.amount(), AmountContext.forStackEntry(entry, null));
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, manifested, e.counterType(), count);
        }
    }
}
