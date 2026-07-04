package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeparatePermanentsIntoPilesAndSacrificeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeparatePermanentsIntoPilesAndSacrificeEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeparatePermanentsIntoPilesAndSacrificeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
                if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                    return;
                }
                UUID controllerId = entry.getControllerId();

                List<Permanent> permanents = gameData.playerBattlefields.get(targetPlayerId);
                if (permanents == null || permanents.isEmpty()) {
                    String playerName = gameData.playerIdToName.get(targetPlayerId);
                    gameBroadcastService.logAndBroadcast(gameData, playerName + " has no permanents to separate.");
                    log.info("Game {} - {} has no permanents to separate", gameData.id, playerName);
                    return;
                }

                List<UUID> allPermanentIds = permanents.stream().map(Permanent::getId).toList();

                // Store pile separation state
                gameData.queueInteraction(new PendingPileSeparation(controllerId, targetPlayerId,
                        allPermanentIds, List.of(), Map.of(), List.of(), List.of()));

                // Prompt the controller to choose permanents for pile 1
                playerInputService.beginMultiPermanentChoice(gameData, controllerId, allPermanentIds,
                        allPermanentIds.size(),
                        "Separate permanents into two piles. Select permanents for Pile 1 (unselected form Pile 2).");
    }
}
