package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentWhileSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GainControlOfTargetPermanentWhileSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetPermanentWhileSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) return;

                UUID sourcePermanentId = entry.getSourcePermanentId();
                if (sourcePermanentId == null) return;

                // Per ruling: if you lose control of the source permanent before this resolves,
                // the ability resolves with no effect.
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (source == null) {
                    String logEntry = entry.getCard().getName() + "'s ability has no effect (source left the battlefield).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    return;
                }
                UUID sourceController = gameQueryService.findPermanentController(gameData, sourcePermanentId);
                if (sourceController == null || !sourceController.equals(entry.getControllerId())) {
                    String logEntry = entry.getCard().getName() + "'s ability has no effect (controller no longer controls " + source.getCard().getName() + ").";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    return;
                }

                UUID oldController = gameQueryService.findPermanentController(gameData, target.getId());
                if (oldController != null && !oldController.equals(entry.getControllerId())) {
                    creatureControlService.stealPermanent(gameData, entry.getControllerId(), target);
                    gameData.sourceDependentStolenCreatures.put(target.getId(), sourcePermanentId);
                }
    
    }
}
