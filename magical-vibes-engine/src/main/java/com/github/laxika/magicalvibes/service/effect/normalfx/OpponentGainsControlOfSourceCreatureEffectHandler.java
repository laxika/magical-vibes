package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link OpponentGainsControlOfSourceCreatureEffect}: an opponent of the ability's
 * controller gains control of the source permanent. Also reachable as a
 * {@link com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect} fallback.
 */
@Component
@RequiredArgsConstructor
public class OpponentGainsControlOfSourceCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentGainsControlOfSourceCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (OpponentGainsControlOfSourceCreatureEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, opponentId, source,
                new GainControlOfTargetEffect(e.duration()),
                EffectDuration.PERMANENT, null, entry.getCard().getName());
    }
}
