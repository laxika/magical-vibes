package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves per-player coin flips and the resulting simultaneous sacrifice choices. */
@Component
@RequiredArgsConstructor
public class EachPlayerFlipsCoinAndTailsSacrificesCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final CoinFlipService coinFlipService;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> autoSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();
        String sourceName = entry.getCard().getName();

        for (UUID playerId : apnapPlayers(gameData)) {
            CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            String outcome = result.heads() ? "wins" : "loses";
            gameLogService.append(gameData, GameLog.text(playerName + " " + outcome
                    + " the coin flip for " + sourceName
                    + coinFlipService.replacementDetails(result) + "."));

            if (result.heads()) {
                triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, playerId);
                continue;
            }

            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
                continue;
            }

            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, playerId,
                    permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent));
            if (creatureIds.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + " has no creature to sacrifice."));
            } else if (creatureIds.size() == 1) {
                autoSacrificeIds.add(creatureIds.getFirst());
            } else {
                choosers.add(new PendingForcedSacrifice(playerId, 1, creatureIds));
            }
        }

        if (choosers.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, autoSacrificeIds);
        } else {
            destructionSupport.beginNextForcedSacrificeFromQueue(
                    gameData, choosers, autoSacrificeIds, true);
        }
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}
