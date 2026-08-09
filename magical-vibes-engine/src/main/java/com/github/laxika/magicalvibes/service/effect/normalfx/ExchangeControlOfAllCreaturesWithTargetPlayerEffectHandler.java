package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfAllCreaturesWithTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves the temporary symmetric creature-control exchange used by Reins of Power.
 */
@Component
@RequiredArgsConstructor
public class ExchangeControlOfAllCreaturesWithTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeControlOfAllCreaturesWithTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null
                || targetPlayerId.equals(controllerId)
                || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> controllerCreatures = creaturesControlledBy(gameData, controllerId);
        List<Permanent> targetCreatures = creaturesControlledBy(gameData, targetPlayerId);
        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.END_OF_TURN);

        for (Permanent creature : targetCreatures) {
            creatureControlService.applyControlEffect(gameData, controllerId, creature,
                    controlEffect, ControlDuration.END_OF_TURN.toEffectDuration(), null,
                    entry.getCard().getName());
        }
        for (Permanent creature : controllerCreatures) {
            creatureControlService.applyControlEffect(gameData, targetPlayerId, creature,
                    controlEffect, ControlDuration.END_OF_TURN.toEffectDuration(), null,
                    entry.getCard().getName());
        }
    }

    private List<Permanent> creaturesControlledBy(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }

        List<Permanent> creatures = new ArrayList<>();
        for (Permanent permanent : List.copyOf(battlefield)) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatures.add(permanent);
            }
        }
        return creatures;
    }
}
