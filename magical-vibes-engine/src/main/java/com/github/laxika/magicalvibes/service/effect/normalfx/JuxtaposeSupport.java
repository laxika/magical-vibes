package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Drives Juxtapose: the spell's controller and the target player exchange control of the creature
 * they each control with the greatest mana value, then exchange artifacts the same way.
 *
 * <p>The two exchanges are handled one type at a time (creatures first, then artifacts on the board
 * left after the creature swap), so an artifact creature can be moved in both steps. For each type,
 * the controller's participant is determined first, then the target player's; a tie for greatest
 * mana value prompts that permanent's controller to choose one (CR/Gatherer ruling). If either
 * player controls no permanent of the current type, that exchange is skipped entirely.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JuxtaposeSupport {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    /** Entry point from the effect handler: begin with the creature exchange. */
    public void begin(GameData gameData, Card sourceCard, UUID controllerId, UUID targetPlayerId) {
        resolveControllerSide(gameData, sourceCard, controllerId, targetPlayerId, false);
    }

    /** Determine (or prompt for) the controller's participant for the current phase. */
    private void resolveControllerSide(GameData gameData, Card sourceCard, UUID controllerId,
                                       UUID targetPlayerId, boolean artifactPhase) {
        List<Permanent> tied = tiedForGreatest(gameData, controllerId, artifactPhase);
        if (tied.isEmpty()) {
            nextPhase(gameData, sourceCard, controllerId, targetPlayerId, artifactPhase);
            return;
        }
        if (tied.size() > 1) {
            promptChoice(gameData, sourceCard, controllerId, targetPlayerId, artifactPhase, false, null,
                    controllerId, tied);
            return;
        }
        resolveTargetSide(gameData, sourceCard, controllerId, targetPlayerId, artifactPhase, tied.getFirst().getId());
    }

    /** Determine (or prompt for) the target player's participant, then exchange. */
    private void resolveTargetSide(GameData gameData, Card sourceCard, UUID controllerId,
                                   UUID targetPlayerId, boolean artifactPhase, UUID controllerPermanentId) {
        List<Permanent> tied = tiedForGreatest(gameData, targetPlayerId, artifactPhase);
        if (tied.isEmpty()) {
            nextPhase(gameData, sourceCard, controllerId, targetPlayerId, artifactPhase);
            return;
        }
        if (tied.size() > 1) {
            promptChoice(gameData, sourceCard, controllerId, targetPlayerId, artifactPhase, true,
                    controllerPermanentId, targetPlayerId, tied);
            return;
        }
        exchange(gameData, sourceCard, controllerId, targetPlayerId, controllerPermanentId, tied.getFirst().getId());
        nextPhase(gameData, sourceCard, controllerId, targetPlayerId, artifactPhase);
    }

    /** Completion of a tie-break prompt. */
    public void handleTieBreakChosen(GameData gameData, UUID permanentId,
                                     PermanentChoiceContext.JuxtaposeTieBreak context) {
        if (!context.controllerChosen()) {
            resolveTargetSide(gameData, context.sourceCard(), context.controllerId(),
                    context.targetPlayerId(), context.artifactPhase(), permanentId);
        } else {
            exchange(gameData, context.sourceCard(), context.controllerId(), context.targetPlayerId(),
                    context.controllerPermanentId(), permanentId);
            nextPhase(gameData, context.sourceCard(), context.controllerId(),
                    context.targetPlayerId(), context.artifactPhase());
        }
    }

    private void promptChoice(GameData gameData, Card sourceCard, UUID controllerId, UUID targetPlayerId,
                              boolean artifactPhase, boolean controllerChosen, UUID controllerPermanentId,
                              UUID choosingPlayerId, List<Permanent> tied) {
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.JuxtaposeTieBreak(
                sourceCard, controllerId, targetPlayerId, artifactPhase, controllerChosen, controllerPermanentId));
        String type = artifactPhase ? "artifact" : "creature";
        playerInputService.beginPermanentChoice(gameData, choosingPlayerId,
                tied.stream().map(Permanent::getId).toList(),
                "Choose which " + type + " to exchange (" + sourceCard.getName() + ").");
    }

    private void exchange(GameData gameData, Card sourceCard, UUID controllerId, UUID targetPlayerId,
                          UUID controllerPermanentId, UUID targetPermanentId) {
        Permanent controllerPerm = gameQueryService.findPermanentById(gameData, controllerPermanentId);
        Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (controllerPerm == null || targetPerm == null) {
            return;
        }

        GainControlOfTargetEffect control = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        creatureControlService.applyControlEffect(gameData, targetPlayerId, controllerPerm, control,
                ControlDuration.PERMANENT.toEffectDuration(), null, sourceCard.getName());
        creatureControlService.applyControlEffect(gameData, controllerId, targetPerm, control,
                ControlDuration.PERMANENT.toEffectDuration(), null, sourceCard.getName());

        String logEntry = sourceCard.getName() + ": " + controllerPerm.getCard().getName() + " and "
                + targetPerm.getCard().getName() + " exchange controllers.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exchanges control of {} and {}", gameData.id, sourceCard.getName(),
                controllerPerm.getCard().getName(), targetPerm.getCard().getName());
    }

    private void nextPhase(GameData gameData, Card sourceCard, UUID controllerId, UUID targetPlayerId,
                           boolean artifactPhase) {
        if (!artifactPhase) {
            resolveControllerSide(gameData, sourceCard, controllerId, targetPlayerId, true);
        } else {
            finish(gameData);
        }
    }

    private void finish(GameData gameData) {
        if (gameData.pendingEffectResolutionEntry != null) {
            // Reached asynchronously via a completed prompt — finalize the spell (SBA, graveyard).
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
        // Otherwise we are still inside the initial synchronous effect resolution; returning lets
        // EffectResolutionService finalize the spell.
    }

    /** The permanents {@code playerId} controls of the current type that are tied for greatest mana value. */
    private List<Permanent> tiedForGreatest(GameData gameData, UUID playerId, boolean artifactPhase) {
        List<Permanent> matching = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                boolean matches = artifactPhase
                        ? gameQueryService.isArtifact(gameData, perm)
                        : gameQueryService.isCreature(gameData, perm);
                if (matches) {
                    matching.add(perm);
                }
            }
        }
        if (matching.isEmpty()) {
            return matching;
        }
        int maxManaValue = matching.stream().mapToInt(p -> p.getCard().getManaValue()).max().orElse(0);
        return matching.stream()
                .filter(p -> p.getCard().getManaValue() == maxManaValue)
                .toList();
    }
}
