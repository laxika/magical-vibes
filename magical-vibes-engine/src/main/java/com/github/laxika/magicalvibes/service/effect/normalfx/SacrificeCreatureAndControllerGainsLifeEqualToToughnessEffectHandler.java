package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
                boolean sacrificerIsController =
                        ((SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect) effect).sacrificerIsController();
                UUID targetPlayerId = sacrificerIsController ? controllerId : entry.getTargetId();
                if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                    return;
                }

                String cardName = entry.getCard().getName();

                List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, targetPlayerId, p -> true);

                if (creatureIds.isEmpty()) {
                    String playerName = gameData.playerIdToName.get(targetPlayerId);
                    String logEntry = playerName + " has no creatures to sacrifice.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} has no creatures to sacrifice", gameData.id, playerName);
                    return;
                }

                if (creatureIds.size() == 1) {
                    Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
                    if (creature != null) {
                        int toughness = gameQueryService.getEffectiveToughness(gameData, creature);
                        destructionSupport.sacrificeAndLog(gameData, creature, targetPlayerId);
                        lifeSupport.applyGainLife(gameData, controllerId, toughness, cardName);
                    }
                    return;
                }

                // Multiple creatures — prompt player to choose
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness(
                                targetPlayerId, controllerId, cardName));
                playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                        "Choose a creature to sacrifice.");
    }
}
