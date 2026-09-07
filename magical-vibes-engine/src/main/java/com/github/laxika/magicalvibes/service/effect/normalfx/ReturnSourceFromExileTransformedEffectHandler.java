package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.service.battlefield.ExileAndReturnTransformedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a craft ability's return of its source card from exile. */
@Component
@RequiredArgsConstructor
public class ReturnSourceFromExileTransformedEffectHandler implements NormalEffectHandlerBean {

    private final ExileAndReturnTransformedService exileAndReturnTransformedService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceFromExileTransformedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCard() != null) {
            exileAndReturnTransformedService.returnTransformedFromExile(
                    gameData, entry.getCard().getId(), entry.getSourcePermanentId());
        }
    }
}
