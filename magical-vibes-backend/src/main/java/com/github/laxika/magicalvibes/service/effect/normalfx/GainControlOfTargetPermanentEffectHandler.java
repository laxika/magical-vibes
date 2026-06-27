package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GainControlOfTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainControlOfTargetPermanentEffect) effect;
        
                List<UUID> targetIds = entry.getTargetIds().isEmpty()
                        ? (entry.getTargetId() != null ? List.of(entry.getTargetId()) : List.of())
                        : entry.getTargetIds();

                for (UUID targetId : targetIds) {
                    Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                    if (target == null) continue;

                    UUID oldController = gameQueryService.findPermanentController(gameData, target.getId());
                    if (oldController != null && !oldController.equals(entry.getControllerId())) {
                        creatureControlService.stealPermanent(gameData, entry.getControllerId(), target);
                        gameData.permanentControlStolenCreatures.add(target.getId());
                    }

                    if (e.grantedSubtype() != null && !target.getGrantedSubtypes().contains(e.grantedSubtype())) {
                        target.getGrantedSubtypes().add(e.grantedSubtype());
                        String subtypeLog = target.getCard().getName() + " becomes a " + e.grantedSubtype().getDisplayName() + " in addition to its other types.";
                        gameBroadcastService.logAndBroadcast(gameData, subtypeLog);
                    }
                }
    
    }
}
