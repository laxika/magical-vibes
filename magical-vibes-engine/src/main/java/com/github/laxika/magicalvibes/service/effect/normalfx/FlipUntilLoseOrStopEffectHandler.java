package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseOrStopEffect;
import com.github.laxika.magicalvibes.service.effect.ChoosesCoinFlipResultsFirst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves {@link FlipUntilLoseOrStopEffect}'s first coin flip. */
@Component
@RequiredArgsConstructor
public class FlipUntilLoseOrStopEffectHandler implements NormalEffectHandlerBean, ChoosesCoinFlipResultsFirst {

    private final FlipUntilLoseOrStopSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipUntilLoseOrStopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        FlipUntilLoseOrStopEffect flipEffect = (FlipUntilLoseOrStopEffect) effect;
        if (!support.flip(gameData, entry.getControllerId(), entry.getCard().getName())) {
            return;
        }

        support.queueContinue(gameData, entry.getCard(), entry.getControllerId(), entry.getTargetId(),
                1, flipEffect.rewards());
    }
}
