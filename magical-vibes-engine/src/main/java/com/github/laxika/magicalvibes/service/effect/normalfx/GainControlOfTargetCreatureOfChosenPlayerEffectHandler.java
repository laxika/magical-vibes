package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureOfChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GainControlOfTargetCreatureOfChosenPlayerEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetCreatureOfChosenPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var control = (GainControlOfTargetCreatureOfChosenPlayerEffect) effect;
        UUID chosenPlayerId = entry.targetsForGroup(0).stream().findFirst().orElse(null);
        UUID targetId = entry.targetsForGroup(control.targetGroup()).stream().findFirst().orElse(null);
        if (chosenPlayerId == null || targetId == null
                || !gameData.playerIds.contains(chosenPlayerId)
                || chosenPlayerId.equals(entry.getControllerId())) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null
                || !gameQueryService.isCreature(gameData, target)
                || !chosenPlayerId.equals(gameQueryService.findPermanentController(gameData, targetId))) {
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || !source.isTapped()) {
            return;
        }

        creatureControlService.applyControlEffect(
                gameData,
                entry.getControllerId(),
                target,
                new GainControlOfTargetEffect(control.duration()),
                control.duration().toEffectDuration(),
                sourcePermanentId,
                entry.getCard().getName());
    }
}
