package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturesByPositionEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BoostTargetCreaturesByPositionEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final BoostTargetCreatureEffectHandler boostTargetCreatureEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetCreaturesByPositionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var positionalBoost = (BoostTargetCreaturesByPositionEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(positionalBoost);
        List<BoostTargetCreatureEffect> boosts = positionalBoost.boosts();

        for (int position = 0; position < targetIds.size() && position < boosts.size(); position++) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetIds.get(position));
            if (target != null) {
                boostTargetCreatureEffectHandler.resolveForTarget(gameData, entry, target, boosts.get(position));
            }
        }
    }
}
