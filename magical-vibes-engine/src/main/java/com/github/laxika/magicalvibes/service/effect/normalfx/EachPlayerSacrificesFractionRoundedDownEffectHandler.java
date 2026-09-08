package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesFractionRoundedDownEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves each player's rounded-down fraction sacrifice choice. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerSacrificesFractionRoundedDownEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesFractionRoundedDownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerSacrificesFractionRoundedDownEffect) effect;
        List<UUID> autoSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();

        for (UUID playerId : orderedApnap(gameData)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || battlefield.isEmpty()) {
                continue;
            }

            List<Permanent> matching = battlefield.stream()
                    .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, e.filter()))
                    .toList();
            int count = matching.size() / e.divisor();
            if (count <= 0) {
                continue;
            }
            if (matching.size() <= count) {
                matching.stream().map(Permanent::getId).forEach(autoSacrificeIds::add);
            } else {
                choosers.add(new PendingForcedSacrifice(playerId, count,
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
        log.info("Game {} - {} sacrifices a rounded-down fraction of matching permanents",
                gameData.id, entry.getCard().getName());
    }

    private List<UUID> orderedApnap(GameData gameData) {
        List<UUID> ordered = new ArrayList<>();
        if (gameData.orderedPlayerIds.contains(gameData.activePlayerId)) {
            ordered.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                ordered.add(playerId);
            }
        }
        return ordered;
    }
}
