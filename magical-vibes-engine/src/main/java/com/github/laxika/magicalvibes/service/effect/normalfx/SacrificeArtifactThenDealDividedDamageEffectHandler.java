package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeArtifactThenDealDividedDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeArtifactThenDealDividedDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeArtifactThenDealDividedDamageEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<UUID> validArtifactIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isArtifact(p)) {
                    validArtifactIds.add(p.getId());
                }
            }
        }

        if (validArtifactIds.isEmpty()) {
            String logEntry = playerName + " has no artifacts to sacrifice.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no artifacts to sacrifice for {}",
                    gameData.id, playerName, entry.getCard().getName());
            gameData.pendingETBDamageAssignments = Map.of();
            return;
        }

        Map<UUID, Integer> damageAssignments = gameData.pendingETBDamageAssignments;
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeArtifactForDividedDamage(
                        controllerId, entry.getCard(), damageAssignments));
        playerInputService.beginPermanentChoice(gameData, controllerId, validArtifactIds,
                entry.getCard().getName() + " — Choose an artifact to sacrifice.");

        String logEntry = playerName + " is choosing an artifact to sacrifice.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} choosing artifact to sacrifice for divided damage",
                gameData.id, playerName);
    
    }
}
