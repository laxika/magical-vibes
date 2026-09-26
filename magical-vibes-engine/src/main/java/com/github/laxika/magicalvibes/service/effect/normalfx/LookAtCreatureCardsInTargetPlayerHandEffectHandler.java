package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtCreatureCardsInTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LookAtCreatureCardsInTargetPlayerHandEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtCreatureCardsInTargetPlayerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        cardRevealService.lookAtCreatureCardsInHand(
                gameData, entry.getControllerId(), entry.getTargetId());
    }
}
