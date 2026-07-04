package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerSacrificesPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerSacrificesPermanentsEffect) effect;
        // Per CR 101.4 and Destructive Force ruling (2010-08-15): active player chooses first,
                // then each other player in turn order, then all chosen permanents are sacrificed at the
                // same time. Collect all IDs to sacrifice and defer actual sacrifice until all choices
                // are made.
                List<UUID> autoSacrificeIds = new ArrayList<>();
                List<PendingForcedSacrifice> choosers = new ArrayList<>();

                for (UUID playerId : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield == null || battlefield.isEmpty()) {
                        continue;
                    }

                    List<Permanent> matching = battlefield.stream()
                            .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                            .toList();

                    if (matching.isEmpty()) {
                        String playerName = gameData.playerIdToName.get(playerId);
                        String logEntry = playerName + " has no matching permanents to sacrifice.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
                        continue;
                    }

                    if (matching.size() <= e.count()) {
                        // No choice needed — mark all for simultaneous sacrifice
                        matching.stream().map(Permanent::getId)
                                .forEach(autoSacrificeIds::add);
                    } else {
                        // Player must choose — add to queue
                        List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
                        choosers.add(new PendingForcedSacrifice(playerId, e.count(), matchingIds));
                    }
                }

                if (choosers.isEmpty()) {
                    // All players auto-resolved — sacrifice everything now
                    destructionSupport.performSimultaneousSacrifice(gameData, autoSacrificeIds);
                } else {
                    // Some players need to choose — begin the first prompt
                    destructionSupport.beginNextForcedSacrificeFromQueue(gameData, choosers, autoSacrificeIds);
                }
    }
}
