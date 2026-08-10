package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToCountEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link EachPlayerSacrificesDownToCountEffect}. Each player chooses which matching
 * permanents to keep, then the excess permanents are sacrificed simultaneously.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerSacrificesDownToCountEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesDownToCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var downTo = (EachPlayerSacrificesDownToCountEffect) effect;
        List<UUID> ordered = orderedApnap(gameData, gameData.activePlayerId);
        List<UUID> autoSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();

        for (UUID playerId : ordered) {
            List<Permanent> matching = matching(gameData, playerId, downTo);
            int toSacrifice = matching.size() - downTo.count();
            if (toSacrifice <= 0) {
                continue;
            }
            if (toSacrifice >= matching.size()) {
                matching.stream().map(Permanent::getId).forEach(autoSacrificeIds::add);
            } else {
                choosers.add(new PendingForcedSacrifice(playerId, toSacrifice,
                        matching.stream().map(Permanent::getId).toList()));
            }
        }

        if (autoSacrificeIds.isEmpty() && choosers.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.textCardText("No permanents to sacrifice for ", entry.getCard(), "."));
            return;
        }
        if (choosers.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, autoSacrificeIds);
        } else {
            destructionSupport.beginNextForcedSacrificeFromQueue(gameData, choosers, autoSacrificeIds);
        }
        log.info("Game {} - {} sacrifices permanents down to {}", gameData.id, entry.getCard().getName(),
                downTo.count());
    }

    private List<Permanent> matching(GameData gameData, UUID playerId,
            EachPlayerSacrificesDownToCountEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, effect.filter()))
                .toList();
    }

    private List<UUID> orderedApnap(GameData gameData, UUID activePlayerId) {
        List<UUID> ordered = new ArrayList<>();
        if (gameData.orderedPlayerIds.contains(activePlayerId)) {
            ordered.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                ordered.add(playerId);
            }
        }
        return ordered;
    }
}
