package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureDestroyRestEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerChoosesCreatureDestroyRestEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesCreatureDestroyRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> protectedIds = new ArrayList<>();
                List<PendingForcedSacrifice> choosers = new ArrayList<>();

                for (UUID playerId : gameData.orderedPlayerIds) {
                    List<Permanent> creatures = new ArrayList<>();
                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield != null) {
                        for (Permanent perm : battlefield) {
                            if (gameQueryService.isCreature(gameData, perm)) {
                                creatures.add(perm);
                            }
                        }
                    }

                    if (creatures.isEmpty()) {
                        String playerName = gameData.playerIdToName.get(playerId);
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " has no creatures."));
                        continue;
                    }

                    if (creatures.size() == 1) {
                        // Auto-keep the only creature
                        protectedIds.add(creatures.getFirst().getId());
                        String playerName = gameData.playerIdToName.get(playerId);
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " keeps ", creatures.getFirst().getCard(), " (only creature)."));
                        continue;
                    }

                    // Multiple creatures — player must choose 1 to keep
                    List<UUID> creatureIds = creatures.stream().map(Permanent::getId).toList();
                    choosers.add(new PendingForcedSacrifice(playerId, 1, creatureIds));
                }

                if (choosers.isEmpty()) {
                    // All auto-resolved — destroy non-protected creatures now
                    destructionSupport.performDestroyAllCreaturesExcept(gameData, entry.getCard().getName(), protectedIds);
                } else {
                    destructionSupport.beginNextDestroyRestChoice(gameData, choosers, protectedIds,
                            entry.getCard().getName());
                }
    }
}
