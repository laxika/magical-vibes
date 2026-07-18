package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToFewestEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerSacrificesDownToFewestEffect}: every player sacrifices the permanents
 * they control matching the filter down to the number controlled by the player who controls the
 * fewest such permanents. Each player chooses which of their own matching permanents to keep and
 * sacrifices the rest. Uses the APNAP simultaneous forced-sacrifice flow (CR 101.4): the active
 * player chooses first, then each other player in turn order, then all chosen permanents are
 * sacrificed at the same time. Players already at the minimum sacrifice nothing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerSacrificesDownToFewestEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesDownToFewestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerSacrificesDownToFewestEffect) effect;
        UUID activePlayerId = gameData.activePlayerId;
        List<UUID> ordered = orderedApnap(gameData, activePlayerId);

        int fewest = Integer.MAX_VALUE;
        for (UUID playerId : ordered) {
            fewest = Math.min(fewest, matching(gameData, playerId, e).size());
        }
        if (fewest == Integer.MAX_VALUE) {
            fewest = 0;
        }

        List<UUID> autoSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();

        for (UUID playerId : ordered) {
            List<Permanent> matching = matching(gameData, playerId, e);
            int toSacrifice = matching.size() - fewest;
            if (toSacrifice <= 0) {
                continue;
            }
            if (toSacrifice >= matching.size()) {
                matching.stream().map(Permanent::getId).forEach(autoSacrificeIds::add);
            } else {
                List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
                choosers.add(new PendingForcedSacrifice(playerId, toSacrifice, matchingIds));
            }
        }

        if (autoSacrificeIds.isEmpty() && choosers.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.textCardText("No permanents to sacrifice for ", entry.getCard(), "."));
            return;
        }

        if (choosers.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, autoSacrificeIds);
        } else {
            destructionSupport.beginNextForcedSacrificeFromQueue(gameData, choosers, autoSacrificeIds);
        }
    }

    private List<Permanent> matching(GameData gameData, UUID playerId,
            EachPlayerSacrificesDownToFewestEffect e) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return List.of();
        }
        return battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                .toList();
    }

    /** Active player first, then every other player in seating order (CR 101.4 APNAP). */
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
