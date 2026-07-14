package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlUntapAndHasteTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainControlUntapAndHasteTargetEffect} against {@code entry.getTargetId()} by
 * delegating to the existing gain-control, untap, and grant-keyword handlers in turn — the same
 * three effects Threaten composes on its stack entry, executed as one atomic bundle so a single
 * {@link com.github.laxika.magicalvibes.model.effect.MayEffect} can gate all of it.
 */
@Component
@RequiredArgsConstructor
public class GainControlUntapAndHasteTargetEffectHandler implements NormalEffectHandlerBean {

    private final GainControlOfTargetEffectHandler gainControlHandler;
    private final UntapPermanentsEffectHandler untapHandler;
    private final GrantKeywordEffectHandler grantKeywordHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlUntapAndHasteTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gainControlHandler.resolve(gameData, entry, new GainControlOfTargetEffect(ControlDuration.END_OF_TURN));
        untapHandler.resolve(gameData, entry, new UntapPermanentsEffect(TapUntapScope.TARGET));
        grantKeywordHandler.resolve(gameData, entry, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
