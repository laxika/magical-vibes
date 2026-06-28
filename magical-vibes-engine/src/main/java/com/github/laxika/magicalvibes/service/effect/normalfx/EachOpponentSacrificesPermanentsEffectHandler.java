package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EachOpponentSacrificesPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentSacrificesPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOpponentSacrificesPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();

                for (UUID playerId : gameData.orderedPlayerIds) {
                    if (playerId.equals(controllerId)) continue;

                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield == null || battlefield.isEmpty()) {
                        continue;
                    }

                    List<Permanent> matching = battlefield.stream()
                            .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, e.filter()))
                            .toList();

                    if (matching.isEmpty()) {
                        String playerName = gameData.playerIdToName.get(playerId);
                        String logEntry = playerName + " has no matching permanents to sacrifice.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
                        continue;
                    }

                    if (matching.size() <= e.count()) {
                        matching.stream().map(Permanent::getId)
                                .forEach(gameData.pendingSimultaneousSacrificeIds::add);
                    } else {
                        List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
                        gameData.pendingForcedSacrificeQueue.add(
                                new PendingForcedSacrifice(playerId, e.count(), matchingIds));
                    }
                }

                if (gameData.pendingForcedSacrificeQueue.isEmpty()) {
                    destructionSupport.performSimultaneousSacrifice(gameData);
                } else {
                    destructionSupport.beginNextForcedSacrificeFromQueue(gameData);
                }
    }
}
