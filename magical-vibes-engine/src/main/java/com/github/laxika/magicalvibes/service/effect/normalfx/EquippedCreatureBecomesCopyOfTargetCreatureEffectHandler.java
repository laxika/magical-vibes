package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EquippedCreatureBecomesCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AuraCopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EquippedCreatureBecomesCopyOfTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraCopyService auraCopyService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EquippedCreatureBecomesCopyOfTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent equipped = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        Permanent chosen = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        auraCopyService.applyEquippedCreatureCopy(gameData, equipment, equipped, chosen);
    }
}
