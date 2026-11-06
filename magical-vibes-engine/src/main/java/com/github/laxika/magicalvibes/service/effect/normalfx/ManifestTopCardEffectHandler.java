package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves manifesting the top cards of a library, one at a time (CR 701.40e). */
@Component
@RequiredArgsConstructor
public class ManifestTopCardEffectHandler implements NormalEffectHandlerBean {

    private final ManifestService manifestService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManifestTopCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int amount = amountEvaluationService.evaluate(gameData, ((ManifestTopCardEffect) effect).amount(),
                AmountContext.forStackEntry(entry, null));
        for (int i = 0; i < amount; i++) {
            manifestService.manifestTopCard(gameData, entry.getControllerId(), entry.getCard());
        }
    }
}
