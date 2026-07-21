package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SoulbondPairWithEnteringEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SoulbondSupport soulbondSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SoulbondPairWithEnteringEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent entering = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || entering == null) {
            return;
        }
        UUID controllerId = entry.getControllerId();
        if (!controllerId.equals(gameQueryService.findPermanentController(gameData, source.getId()))
                || !controllerId.equals(gameQueryService.findPermanentController(gameData, entering.getId()))) {
            return;
        }
        if (!soulbondSupport.isUnpairedCreature(gameData, source)
                || !soulbondSupport.isUnpairedCreature(gameData, entering)) {
            return;
        }
        soulbondSupport.pair(gameData, source, entering);
    }
}
