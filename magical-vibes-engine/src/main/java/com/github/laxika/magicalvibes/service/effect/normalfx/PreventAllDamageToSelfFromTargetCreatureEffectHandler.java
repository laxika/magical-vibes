package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TargetSourceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToSelfFromTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventAllDamageToSelfFromTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventAllDamageToSelfFromTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        UUID targetId = entry.getTargetId();
        if (sourceId == null || targetId == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (source == null || target == null) return;

        gameData.targetSourceDamagePreventionShields.add(
                TargetSourceDamagePreventionShield.allDamage(sourceId, targetId));
        gameLogService.append(gameData, GameLog.text(
                "All damage that would be dealt to " + source.getCard().getName() + " by "
                        + target.getCard().getName() + " this turn is prevented."));
    }
}
