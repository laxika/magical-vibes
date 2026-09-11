package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkIfPlanarSourceHasCountersEffect;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaneswalkIfPlanarSourceHasCountersEffectHandler implements NormalEffectHandlerBean {

    private final PlanechaseService planechaseService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlaneswalkIfPlanarSourceHasCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.planechase == null || entry.getSourcePlanarObject() == null) {
            return;
        }

        PlanarObject source = gameData.planechase.faceUp.stream()
                .filter(object -> object.getId().equals(entry.getSourcePlanarObject().getId()))
                .findFirst()
                .orElse(null);
        if (source == null) {
            return;
        }

        var threshold = (PlaneswalkIfPlanarSourceHasCountersEffect) effect;
        if (source.getCounters().getOrDefault(threshold.counterType(), 0) >= threshold.threshold()) {
            planechaseService.planeswalk(gameData);
        }
    }
}
