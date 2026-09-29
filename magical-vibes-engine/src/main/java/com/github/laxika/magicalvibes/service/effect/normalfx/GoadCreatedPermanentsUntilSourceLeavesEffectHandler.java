package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadCreatedPermanentsUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves source-bound goad for permanents created earlier in the same resolution. */
@Component
@RequiredArgsConstructor
public class GoadCreatedPermanentsUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoadCreatedPermanentsUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        for (UUID createdPermanentId : List.copyOf(entry.getCreatedPermanentIds())) {
            if (gameQueryService.findPermanentById(gameData, createdPermanentId) == null) {
                continue;
            }
            GoadCreaturesUntilNextTurnEffect goad = new GoadCreaturesUntilNextTurnEffect(
                    new PermanentIsSpecificPermanentPredicate(createdPermanentId));
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard() == null ? "Goad" : entry.getCard().getName(),
                    entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    goad,
                    createdPermanentId,
                    null,
                    null,
                    EffectDuration.WHILE_SOURCE_REMAINS,
                    0));
        }
    }
}
