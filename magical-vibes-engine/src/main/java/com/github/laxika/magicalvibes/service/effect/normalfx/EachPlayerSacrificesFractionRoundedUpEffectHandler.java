package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesFractionRoundedUpEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerSacrificesFractionRoundedUpEffect}: each player sacrifices
 * {@code ceil(matching / divisor)} of the permanents they control matching the filter, chosen by
 * that player, where the count is recomputed against that player's own matching permanents. Uses
 * the APNAP simultaneous forced-sacrifice flow (CR 101.4): active player chooses first, then each
 * other player in turn order, then all chosen permanents are sacrificed at the same time. Players
 * with fewer matching permanents than their count sacrifice all of them (no choice needed).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerSacrificesFractionRoundedUpEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesFractionRoundedUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerSacrificesFractionRoundedUpEffect) effect;
        UUID activePlayerId = gameData.activePlayerId;

        List<UUID> autoSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();

        for (UUID playerId : orderedApnap(gameData, activePlayerId)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || battlefield.isEmpty()) {
                continue;
            }

            List<Permanent> matching = battlefield.stream()
                    .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                    .toList();

            if (matching.isEmpty()) {
                continue;
            }

            int count = (matching.size() + e.divisor() - 1) / e.divisor();
            if (matching.size() <= count) {
                matching.stream().map(Permanent::getId).forEach(autoSacrificeIds::add);
            } else {
                List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
                choosers.add(new PendingForcedSacrifice(playerId, count, matchingIds));
            }
        }

        if (autoSacrificeIds.isEmpty() && choosers.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text("No permanents to sacrifice for " + entry.getCard().getName() + "."));
            return;
        }

        if (choosers.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, autoSacrificeIds);
        } else {
            destructionSupport.beginNextForcedSacrificeFromQueue(gameData, choosers, autoSacrificeIds);
        }
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
