package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerGainsControlOfSourceCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerGainsControlOfSourceCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayerGainsControlOfSourceCreatureEffect e =
                (TargetPlayerGainsControlOfSourceCreatureEffect) effect;
        
                if (entry.getTargetId() == null || !gameData.playerIds.contains(entry.getTargetId())) {
                    return;
                }

                UUID newControllerId = entry.getTargetId();
                Permanent source = entry.getSourcePermanentId() == null
                        ? null
                        : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());

                if (source == null) {
                    gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability has no effect (it is no longer on the battlefield)."));
                    return;
                }

                creatureControlService.applyControlEffect(gameData, newControllerId, source,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT, null, entry.getCard().getName());

                if (e.thenEffect() != null) {
                    int effectIndex = entry.getEffectsToResolve().indexOf(effect);
                    if (effectIndex >= 0) {
                        entry.insertEffectsToResolve(effectIndex + 1, List.of(e.thenEffect()));
                    }
                }
    }
}
