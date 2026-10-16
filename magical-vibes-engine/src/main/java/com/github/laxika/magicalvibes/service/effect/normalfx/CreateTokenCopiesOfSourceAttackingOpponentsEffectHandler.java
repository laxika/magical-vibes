package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSourceAttackingOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.EncoreEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Encore (CR 702.141a): the copies are created untapped and not attacking; each must attack its opponent
 * this turn if able. Resolution is shared with {@link EncoreEffectHandler}.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfSourceAttackingOpponentsEffectHandler implements NormalEffectHandlerBean {

    private final EncoreEffectHandler encoreEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfSourceAttackingOpponentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        encoreEffectHandler.resolve(gameData, entry, new EncoreEffect());
    }
}
