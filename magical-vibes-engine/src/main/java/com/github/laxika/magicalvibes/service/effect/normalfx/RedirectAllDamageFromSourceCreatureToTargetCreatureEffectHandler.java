package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageFromSourceCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedirectAllDamageFromSourceCreatureToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectAllDamageFromSourceCreatureToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        UUID targetId = entry.getTargetId();
        Permanent source = sourceId == null ? null : gameQueryService.findPermanentById(gameData, sourceId);
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (source == null || target == null
                || !gameQueryService.isCreature(gameData, source)
                || !gameQueryService.isCreature(gameData, target)
                || sourceId.equals(targetId)) {
            return;
        }

        gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                sourceId, null, CreatureDamageRedirectShield.UNLIMITED, targetId));
    }
}
