package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RandomPlayerDiscardsAndLosesLifeIfLandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RandomPlayerDiscardsAndLosesLifeIfLandEffectHandler implements NormalEffectHandlerBean {

    private static final DiscardCardThenEffect DISCARD_EFFECT = new DiscardCardThenEffect(
            new CardTruePredicate(),
            new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER),
            "a card",
            new CardTypePredicate(CardType.LAND),
            true,
            null,
            null,
            DiscardRecipient.TARGET_PLAYER,
            true);

    private final DiscardCardThenEffectHandler discardCardThenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RandomPlayerDiscardsAndLosesLifeIfLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        discardCardThenEffectHandler.resolve(gameData, entry, DISCARD_EFFECT);
    }
}
