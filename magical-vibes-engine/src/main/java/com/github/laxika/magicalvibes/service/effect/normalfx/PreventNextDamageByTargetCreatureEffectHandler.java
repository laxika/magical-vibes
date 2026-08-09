package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Awe Strike's target-creature source shield. */
@Component
@RequiredArgsConstructor
public class PreventNextDamageByTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageByTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (targetId == null || controllerId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        gameData.sourceNextDamageToAnyTargetShields.add(
                SourceNextDamageToAnyTargetShield.withLifeGain(targetId, controllerId));
        gameLogService.append(gameData, GameLog.textCardText(
                "The next time ", target.getCard(), " would deal damage this turn, it is prevented. "
                        + "The controller gains life equal to the damage prevented."));
    }
}
