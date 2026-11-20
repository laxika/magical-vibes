package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileTargetPermanentEffect}. A plain {@code target()} keeps the target path: every chosen target of
 * the effect's group, its controller and last-known card and power recorded for later steps, and the token for its
 * controller. "That creature" ({@code triggering()}) takes the same path. Every other subject ("this permanent",
 * "enchanted creature") is found by {@link SubjectResolutionService}; a permanent that already left is not exiled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final DestructionSupport destructionSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileTargetPermanentEffect) effect;
        PermanentSubject subject = exile.subject();
        if (!(subject instanceof PermanentSubject.Triggering)
                && !(subject instanceof PermanentSubject.Target target && target.group() == null)) {
            exileSubject(gameData, entry, exile);
            return;
        }
        List<UUID> exiled = new java.util.ArrayList<>();
        List<UUID> exiledControllers = new java.util.ArrayList<>();
        // Multi-target / optional group: exile each chosen target for this effect's group
        // (targetsForEffect). An empty group ("any number" / "up to N" with none chosen) exiles
        // nothing — do not fall back to List.of(null) when targetId is unset.
        // "Exile that creature" (triggering()): the triggering permanent, if it is still on the battlefield.
        boolean triggering = subject instanceof PermanentSubject.Triggering;
        List<UUID> targetIds = triggering
                ? java.util.Collections.singletonList(entry.getTriggeringPermanentId())
                : entry.targetsForEffect(effect);
        if (triggering) {
            targetIds = targetIds.get(0) == null ? List.of() : targetIds;
        } else if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getCard().getTargetGroups().size() == 1) {
            targetIds = entry.getTargetIds();
        }

        List<UUID> tokenControllers = new java.util.ArrayList<>();
        for (UUID targetId : targetIds) {
            if (targetId == null) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            // Capture the controller before exile (needed for token creation)
            UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());

            if (controllerId != null) {
                entry.getRemovedPermanentControllers().put(targetId, controllerId);
            }
            // Later steps of the same resolution read the exiled permanent's last known information.
            entry.rememberLastKnownPermanentCard(targetId, target.getCard());
            entry.getLastKnownPermanentPowers().put(targetId, gameQueryService.getEffectivePower(gameData, target));
            if (permanentRemovalService.removePermanentToExile(gameData, target)) {
                exiled.add(targetId);
                if (controllerId != null) {
                    exiledControllers.add(controllerId);
                }
            }
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, target.getCard().getName(), entry.getCard().getName());

            // Create a token for the exiled permanent's controller if specified
            if (exile.tokenForController() != null && controllerId != null) {
                tokenControllers.add(controllerId);
            }
        }

        entry.recordStep(StepKind.EXILE, StepOutcome.ofObjects(exiled, exiledControllers));
        permanentRemovalService.removeOrphanedAuras(gameData);
        for (UUID controllerId : tokenControllers) {
            destructionSupport.createTokenForPlayer(gameData, controllerId, exile.tokenForController(),
                    entry.getCard().getName(), entry.getCard().getSetCode());
        }
    }

    private void exileSubject(GameData gameData, StackEntry entry, ExileTargetPermanentEffect exile) {
        List<UUID> exiled = new java.util.ArrayList<>();
        List<UUID> exiledControllers = new java.util.ArrayList<>();
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, exile, exile.subject())) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (exile.outsideGame()) {
                if (!permanentRemovalService.removePermanentToOutsideGame(gameData, permanent)) {
                    continue;
                }
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is removed from the game."));
            } else {
                if (!permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                    continue;
                }
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                exiled.add(permanent.getId());
                if (controllerId != null) {
                    exiledControllers.add(controllerId);
                }
            }
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, permanent.getCard().getName(), entry.getCard().getName());
        }
        entry.recordStep(StepKind.EXILE, StepOutcome.ofObjects(exiled, exiledControllers));
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
