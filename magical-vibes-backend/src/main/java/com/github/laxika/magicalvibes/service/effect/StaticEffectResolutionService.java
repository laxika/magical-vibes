package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaticEffectResolutionService {

    private final GameQueryService gameQueryService;
    private final StaticEffectSupport support;

}
