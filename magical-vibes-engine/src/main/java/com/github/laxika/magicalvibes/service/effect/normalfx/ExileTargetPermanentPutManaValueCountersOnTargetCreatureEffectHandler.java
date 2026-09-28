package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the two independent targets of Ruinous Intrusion. */
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffect) effect;
        List<UUID> permanentTargets = entry.targetsForGroup(exileEffect.permanentTargetGroup());
        if (permanentTargets.isEmpty()) {
            return;
        }

        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, permanentTargets.getFirst());
        if (targetPermanent == null) {
            return;
        }

        int counters = Math.max(0, targetPermanent.getCard().getManaValue());
        permanentRemovalService.removePermanentToExile(gameData, targetPermanent);
        gameLogService.append(gameData, GameLog.cardThen(targetPermanent.getCard(), " is exiled."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (counters == 0) {
            return;
        }

        List<UUID> creatureTargets = entry.targetsForGroup(exileEffect.creatureTargetGroup());
        if (creatureTargets.isEmpty()) {
            return;
        }
        Permanent targetCreature = gameQueryService.findPermanentById(gameData, creatureTargets.getFirst());
        if (targetCreature == null || !gameQueryService.isCreature(gameData, targetCreature)
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(
                        gameData, targetCreature.getId()))) {
            return;
        }

        permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, targetCreature, counters);
    }
}
