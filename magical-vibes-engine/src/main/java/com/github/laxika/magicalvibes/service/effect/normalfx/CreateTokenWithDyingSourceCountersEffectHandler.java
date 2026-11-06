package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CreateTokenWithDyingSourceCountersEffect} (Ambitious Augmenter): the token and then a
 * {@link PutCountersOnCreatedPermanentsEffect} for each counter type the stack entry's triggering permanent had as it
 * last existed on the battlefield (CR 608.2h) resolve next on the entry, in that order, so the token's creation can
 * pause for a replacement choice before the counters go on it.
 */
@Component
public class CreateTokenWithDyingSourceCountersEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenWithDyingSourceCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<CardEffect> steps = new ArrayList<>();
        steps.add(((CreateTokenWithDyingSourceCountersEffect) effect).tokenTemplate());
        entry.triggeringPermanentCounters().forEach((type, count) -> {
            if (count > 0) {
                steps.add(new PutCountersOnCreatedPermanentsEffect(type, new Fixed(count)));
            }
        });
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, steps);
    }
}
