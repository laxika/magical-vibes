package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

@Component
public class BoostNonHumanCreaturesByCreatureTypeCountEffectSelfHandler
        extends BoostNonHumanCreaturesByCreatureTypeCountEffectHandler {

    public BoostNonHumanCreaturesByCreatureTypeCountEffectSelfHandler(
            StaticEffectSupport support, GameQueryService gameQueryService) {
        super(support, gameQueryService);
    }

    @Override
    public boolean selfOnly() {
        return true;
    }
}
