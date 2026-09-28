package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureControlledByPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the target-restricted reflexive fight created after Strax's random selection. */
@Component
@RequiredArgsConstructor
public class SourceFightsTargetCreatureControlledByPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SourceFightsTargetCreatureEffectHandler sourceFightsTargetCreatureEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SourceFightsTargetCreatureControlledByPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var fight = (SourceFightsTargetCreatureControlledByPlayerEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            targetId = entry.targetsForEffect(effect).stream().findFirst().orElse(null);
        }
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null
                || !gameQueryService.isCreature(gameData, target)
                || !fight.playerId().equals(gameQueryService.findPermanentController(gameData, targetId))
                || targetId.equals(fight.excludedPermanentId())) {
            return;
        }

        sourceFightsTargetCreatureEffectHandler.resolve(gameData, entry,
                new SourceFightsTargetCreatureEffect());
    }
}
