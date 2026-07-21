package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileTargetPermanentEffect) effect;
        // Multi-target / optional group: exile each chosen target for this effect's group
        // (targetsForEffect). An empty group ("any number" / "up to N" with none chosen) exiles
        // nothing — do not fall back to List.of(null) when targetId is unset.
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            if (targetId == null) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            // Capture the controller before exile (needed for token creation)
            UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());

            permanentRemovalService.removePermanentToExile(gameData, target);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, target.getCard().getName(), entry.getCard().getName());

            // Create a token for the exiled permanent's controller if specified
            if (exile.tokenForController() != null && controllerId != null) {
                destructionSupport.createTokenForPlayer(gameData, controllerId, exile.tokenForController(), entry.getCard().getName());
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
