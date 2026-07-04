package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DamageSourceControllerSacrificesPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageSourceControllerSacrificesPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DamageSourceControllerSacrificesPermanentsEffect) effect;
        UUID sacrificingPlayerId = e.sacrificingPlayerId();
                int count = e.count();

                if (sacrificingPlayerId == null || count <= 0 || !gameData.playerIds.contains(sacrificingPlayerId)) {
                    return;
                }

                String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
                List<Permanent> battlefield = gameData.playerBattlefields.get(sacrificingPlayerId);
                if (battlefield == null || battlefield.isEmpty()) {
                    String logEntry = playerName + " has no permanents to sacrifice.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} has no permanents to sacrifice", gameData.id, playerName);
                    return;
                }

                List<UUID> permanentIds = battlefield.stream().map(Permanent::getId).toList();

                if (permanentIds.size() <= count) {
                    // Sacrifice all — no choice needed
                    for (Permanent perm : new ArrayList<>(battlefield)) {
                        destructionSupport.sacrificeAndLog(gameData, perm, sacrificingPlayerId);
                    }
                    return;
                }

                // More permanents than required — prompt player to choose
                playerInputService.beginMultiPermanentChoice(gameData, sacrificingPlayerId, permanentIds,
                        count,
                        new MultiPermanentChoiceContext.ForcedSacrifice(sacrificingPlayerId, List.of(), List.of()),
                        "Choose " + count + " permanent" + (count > 1 ? "s" : "") + " to sacrifice.");
    }
}
