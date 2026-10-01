package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.UnleashTheFluxEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Unleash the Flux one iteration at a time so sacrifice choices can pause resolution. */
@Component
public class UnleashTheFluxEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnleashTheFluxEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int effectIndex = entry.getResolvingEffectIndex();
        if (effectIndex < 0) {
            throw new IllegalStateException("UnleashTheFluxEffect is not resolving from its stack entry");
        }

        var nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        entry.insertEffectsToResolve(effectIndex + 1, List.of(
                new SacrificePermanentsEffect(1, nonland, SacrificeRecipient.EACH_PLAYER),
                new FlipCoinWinEffect(null, new UnleashTheFluxEffect())));
    }
}
