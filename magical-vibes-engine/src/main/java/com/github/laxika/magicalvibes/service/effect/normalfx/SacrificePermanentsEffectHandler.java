package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the whole forced-sacrifice family via {@link SacrificePermanentsEffect}: the
 * {@link com.github.laxika.magicalvibes.model.effect.SacrificeRecipient} routes who sacrifices and
 * the filter selects the interaction mechanic.
 *
 * <p>A bare {@link PermanentIsCreaturePredicate} filter routes through the single-select
 * "sacrifice a creature" primitive ({@link DestructionSupport#performSacrificeCreatureForPlayer} +
 * {@code PermanentChoiceContext.SacrificeCreature}) — byte-identical to the old
 * {@code SacrificeCreatureEffect} / {@code ControllerSacrificesCreatureEffect} /
 * {@code EachOpponentSacrificesCreatureEffect} handlers. Any other filter routes through the
 * multi-permanent choice ({@code MultiPermanentChoiceContext.ForcedSacrifice}) — byte-identical to
 * the old {@code TargetPlayerSacrificesPermanentsEffect} / {@code EachPlayerSacrificesPermanentsEffect}
 * / {@code EachOpponentSacrificesPermanentsEffect} handlers. Both mechanics are behaviourally tested
 * and rules-correct ("sacrifice a creature" is always a single creature). The each-player mechanics
 * differ too: creature single-sac loops per player (sequential), permanents use the APNAP
 * simultaneous {@link PendingForcedSacrifice} queue (CR 101.4 / Destructive Force ruling).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificePermanentsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificePermanentsEffect) effect;
        // "Sacrifice a creature" (bare creature filter) uses the single-select creature primitive;
        // every other filter uses the multi-permanent-choice flow. Both are behaviourally tested.
        boolean creatureSingleSac = e.filter() instanceof PermanentIsCreaturePredicate;

        switch (e.recipient()) {
            case CONTROLLER -> resolveSinglePlayer(gameData, entry, e, entry.getControllerId(), creatureSingleSac);
            case TARGET_PLAYER -> {
                UUID targetPlayerId = entry.getTargetId();
                if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                    return;
                }
                resolveSinglePlayer(gameData, entry, e, targetPlayerId, creatureSingleSac);
            }
            case EACH_PLAYER -> resolveEachPlayer(gameData, entry, e, false, creatureSingleSac);
            case EACH_OPPONENT -> resolveEachPlayer(gameData, entry, e, true, creatureSingleSac);
        }
    }

    private void resolveSinglePlayer(GameData gameData, StackEntry entry, SacrificePermanentsEffect e,
            UUID playerId, boolean creatureSingleSac) {
        if (creatureSingleSac) {
            destructionSupport.performSacrificeCreatureForPlayer(gameData, playerId);
            return;
        }

        int count = evaluateCount(gameData, entry, e);

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = playerName + " has no permanents to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no permanents to sacrifice", gameData.id, playerName);
            return;
        }

        List<Permanent> matching = battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                .toList();

        if (matching.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = playerName + " has no matching permanents to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
            return;
        }

        if (matching.size() <= count) {
            // Sacrifice all matching — no choice needed
            for (Permanent perm : matching) {
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
            }
        } else {
            // More matching permanents than required — prompt player to choose
            List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
            playerInputService.beginMultiPermanentChoice(gameData, playerId, matchingIds, count,
                    new MultiPermanentChoiceContext.ForcedSacrifice(playerId, List.of(), List.of()),
                    "Choose " + count + " permanent" + (count > 1 ? "s" : "") + " to sacrifice.");
        }
    }

    private void resolveEachPlayer(GameData gameData, StackEntry entry, SacrificePermanentsEffect e,
            boolean opponentsOnly, boolean creatureSingleSac) {
        UUID controllerId = entry.getControllerId();

        if (creatureSingleSac) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (opponentsOnly && playerId.equals(controllerId)) {
                    continue;
                }
                destructionSupport.performSacrificeCreatureForPlayer(gameData, playerId);
            }
            return;
        }

        // Per CR 101.4 and the Destructive Force ruling (2010-08-15): active player chooses first,
        // then each other player in turn order, then all chosen permanents are sacrificed at the
        // same time. Collect all IDs to sacrifice and defer actual sacrifice until all choices
        // are made.
        int count = evaluateCount(gameData, entry, e);
        List<UUID> autoSacrificeIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (opponentsOnly && playerId.equals(controllerId)) {
                continue;
            }

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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
                continue;
            }

            if (matching.size() <= count) {
                // No choice needed — mark all for simultaneous sacrifice
                matching.stream().map(Permanent::getId).forEach(autoSacrificeIds::add);
            } else {
                // Player must choose — add to queue
                List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
                choosers.add(new PendingForcedSacrifice(playerId, count, matchingIds));
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

    private int evaluateCount(GameData gameData, StackEntry entry, SacrificePermanentsEffect e) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        return amountEvaluationService.evaluate(gameData, e.count(), AmountContext.forStackEntry(entry, source));
    }
}
