package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantExtraLoyaltyActivationToSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a one-shot extra loyalty activation for the source planeswalker only. */
@Component
@RequiredArgsConstructor
public class GrantExtraLoyaltyActivationToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantExtraLoyaltyActivationToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !source.getCard().hasType(CardType.PLANESWALKER)
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, source.getId()))) {
            return;
        }
        source.setExtraLoyaltyActivationsThisTurn(source.getExtraLoyaltyActivationsThisTurn() + 1);
    }
}
