package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
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
        List<UUID> targetIds = entry.getTargetIds().isEmpty()
                ? List.of(entry.getTargetId())
                : entry.getTargetIds();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            // Capture the controller before exile (needed for token creation)
            UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());

            permanentRemovalService.removePermanentToExile(gameData, target);
            String logEntry = target.getCard().getName() + " is exiled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
