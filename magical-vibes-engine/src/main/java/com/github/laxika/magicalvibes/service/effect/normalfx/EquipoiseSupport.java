package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipoisePhase;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Drives Equipoise: for each of land / artifact / creature in order, calculate how many the target
 * player controls in excess of the controller, let the controller choose that many of the target's
 * matching permanents, then phase them out before the next pass. Phasing out mid-sequence updates
 * the board for later passes (artifact creatures chosen as artifacts no longer count as creatures).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquipoiseSupport {

    private final GameQueryService gameQueryService;
    private final PhasingService phasingService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    /** Entry point from the effect handler: begin with the land pass. */
    public void begin(GameData gameData, Card sourceCard, UUID controllerId, UUID targetPlayerId) {
        resolvePhase(gameData, sourceCard, controllerId, targetPlayerId, EquipoisePhase.LAND);
    }

    /** Completion of a multi-permanent choice for the current pass. */
    public void handleChosen(GameData gameData, List<UUID> permanentIds,
                             MultiPermanentChoiceContext.EquipoisePhaseOut context) {
        phaseOutIds(gameData, permanentIds, context.sourceCard());
        advance(gameData, context.sourceCard(), context.controllerId(), context.targetPlayerId(),
                context.phase());
    }

    private void resolvePhase(GameData gameData, Card sourceCard, UUID controllerId,
                              UUID targetPlayerId, EquipoisePhase phase) {
        List<Permanent> controllerMatching = matching(gameData, controllerId, phase);
        List<Permanent> targetMatching = matching(gameData, targetPlayerId, phase);
        int excess = targetMatching.size() - controllerMatching.size();
        if (excess <= 0) {
            advance(gameData, sourceCard, controllerId, targetPlayerId, phase);
            return;
        }

        if (targetMatching.size() <= excess) {
            phaseOutPermanents(gameData, targetMatching, sourceCard);
            advance(gameData, sourceCard, controllerId, targetPlayerId, phase);
            return;
        }

        List<UUID> validIds = targetMatching.stream().map(Permanent::getId).toList();
        String noun = phaseNoun(phase, excess);
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validIds, excess,
                new MultiPermanentChoiceContext.EquipoisePhaseOut(
                        sourceCard, controllerId, targetPlayerId, phase),
                "Choose " + excess + " " + noun + " to phase out (" + sourceCard.getName() + ").");
    }

    private void advance(GameData gameData, Card sourceCard, UUID controllerId, UUID targetPlayerId,
                         EquipoisePhase completedPhase) {
        EquipoisePhase next = switch (completedPhase) {
            case LAND -> EquipoisePhase.ARTIFACT;
            case ARTIFACT -> EquipoisePhase.CREATURE;
            case CREATURE -> null;
        };
        if (next == null) {
            finish(gameData);
        } else {
            resolvePhase(gameData, sourceCard, controllerId, targetPlayerId, next);
        }
    }

    private void finish(GameData gameData) {
        if (gameData.pendingEffectResolutionEntry != null) {
            // Reached asynchronously via a completed prompt — finalize the trigger.
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
        // Otherwise still inside the initial synchronous effect resolution; returning lets
        // EffectResolutionService finalize the entry.
    }

    private void phaseOutIds(GameData gameData, List<UUID> permanentIds, Card sourceCard) {
        List<Permanent> toPhaseOut = new ArrayList<>();
        for (UUID id : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent != null) {
                toPhaseOut.add(permanent);
            }
        }
        phaseOutPermanents(gameData, toPhaseOut, sourceCard);
    }

    private void phaseOutPermanents(GameData gameData, List<Permanent> permanents, Card sourceCard) {
        if (permanents.isEmpty()) {
            return;
        }
        phasingService.phaseOut(gameData, permanents);
        gameLogService.append(gameData, GameLog.builder()
                .card(sourceCard)
                .text(String.format(" phases out %d permanent(s).", permanents.size()))
                .build());
        log.info("Game {} - {} phases out {} permanents", gameData.id, sourceCard.getName(),
                permanents.size());
    }

    private List<Permanent> matching(GameData gameData, UUID playerId, EquipoisePhase phase) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return List.of();
        }
        BiPredicate<GameData, Permanent> matches = phasePredicate(phase);
        List<Permanent> result = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (matches.test(gameData, permanent)) {
                result.add(permanent);
            }
        }
        return result;
    }

    private BiPredicate<GameData, Permanent> phasePredicate(EquipoisePhase phase) {
        return switch (phase) {
            case LAND -> gameQueryService::isLand;
            case ARTIFACT -> gameQueryService::isArtifact;
            case CREATURE -> gameQueryService::isCreature;
        };
    }

    private static String phaseNoun(EquipoisePhase phase, int count) {
        String singular = switch (phase) {
            case LAND -> "land";
            case ARTIFACT -> "artifact";
            case CREATURE -> "creature";
        };
        return count == 1 ? singular : singular + "s";
    }
}
