package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadCreatedPermanentsForRestOfGameEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves goad for permanents created earlier in the same resolution, lasting for the rest of the game. */
@Component
@RequiredArgsConstructor
public class GoadCreatedPermanentsForRestOfGameEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoadCreatedPermanentsForRestOfGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID createdPermanentId : List.copyOf(entry.getCreatedPermanentIds())) {
            if (gameQueryService.findPermanentById(gameData, createdPermanentId) == null) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard() == null ? "Goad" : entry.getCard().getName(),
                    null,
                    entry.getControllerId(),
                    new GoadCreaturesUntilNextTurnEffect(new PermanentIsSpecificPermanentPredicate(createdPermanentId)),
                    createdPermanentId,
                    null,
                    null,
                    EffectDuration.PERMANENT,
                    0));
        }
    }
}
