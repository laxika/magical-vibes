package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesPermanentsEffect;
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
public class TargetPlayerSacrificesPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerSacrificesPermanentsEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
                if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                    return;
                }

                List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
                if (battlefield == null || battlefield.isEmpty()) {
                    String playerName = gameData.playerIdToName.get(targetPlayerId);
                    String logEntry = playerName + " has no permanents to sacrifice.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} has no permanents to sacrifice", gameData.id, playerName);
                    return;
                }

                List<Permanent> matching = battlefield.stream()
                        .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, e.filter()))
                        .toList();

                if (matching.isEmpty()) {
                    String playerName = gameData.playerIdToName.get(targetPlayerId);
                    String logEntry = playerName + " has no matching permanents to sacrifice.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
                    return;
                }

                if (matching.size() <= e.count()) {
                    // Sacrifice all matching — no choice needed
                    for (Permanent perm : matching) {
                        destructionSupport.sacrificeAndLog(gameData, perm, targetPlayerId);
                    }
                } else {
                    // More matching permanents than required — prompt player to choose
                    List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
                    gameData.pendingForcedSacrificeCount = e.count();
                    gameData.pendingForcedSacrificePlayerId = targetPlayerId;
                    playerInputService.beginMultiPermanentChoice(gameData, targetPlayerId, matchingIds,
                            e.count(), "Choose " + e.count() + " permanent"
                                    + (e.count() > 1 ? "s" : "") + " to sacrifice.");
                }
    }
}
