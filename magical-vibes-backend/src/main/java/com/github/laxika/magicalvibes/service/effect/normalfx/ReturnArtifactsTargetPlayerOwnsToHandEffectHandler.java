package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnArtifactsTargetPlayerOwnsToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnArtifactsTargetPlayerOwnsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> artifactsToReturn = new ArrayList<>();
        gameData.forEachBattlefield((controllingPlayerId, battlefield) ->
                artifactsToReturn.addAll(battlefield.stream()
                        .filter(gameQueryService::isArtifact)
                        .filter(p -> {
                            UUID ownerId = gameData.stolenCreatures.getOrDefault(p.getId(), controllingPlayerId);
                            return ownerId.equals(targetPlayerId);
                        })
                        .toList()));

        for (Permanent artifact : artifactsToReturn) {
            permanentRemovalService.removePermanentToHand(gameData, artifact);

            String logEntry = artifact.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, artifact.getCard().getName(), entry.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
