package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainControlOfTargetEffect} for every {@code Duration}. Each
 * resolution creates a floating layer-2 control effect (CR 613.2/613.7) whose duration maps via
 * {@code Duration}; expiry and fallback to the next most recent
 * still-active control effect are handled by {@code CreatureControlService}.
 */
@Component
@RequiredArgsConstructor
public class GainControlOfTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final CreatureControlService creatureControlService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainControlOfTargetEffect) effect;
        Duration duration = e.duration();
        if (Duration.PERMANENT.equals(duration)) {
            resolvePermanent(gameData, entry, e);
        } else if (Duration.UNTIL_END_OF_TURN.equals(duration) || Duration.UNTIL_END_OF_COMBAT.equals(duration)
                || Duration.UNTIL_END_OF_YOUR_NEXT_TURN.equals(duration)) {
            resolveTemporary(gameData, entry, e);
        } else if (Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(duration)) {
            resolveWhileSource(gameData, entry, e, true, false);
        } else if (Duration.WHILE_SOURCE_TAPPED.equals(duration)) {
            resolveWhileSource(gameData, entry, e, true, true);
        } else if (Duration.WHILE_SOURCE_REMAINS.equals(duration)) {
            resolveWhileSource(gameData, entry, e, false, false);
        } else if (Duration.WHILE_SOURCE_REMAINS_TAPPED.equals(duration)) {
            resolveWhileSource(gameData, entry, e, false, true);
        } else {
            throw new IllegalArgumentException("Unsupported control duration: " + duration);
        }
    }

    private void resolvePermanent(GameData gameData, StackEntry entry, GainControlOfTargetEffect e) {
        List<UUID> targetIds = !isTargeted(e) ? named(gameData, entry, e)
                : entry.getTargetIds().isEmpty()
                ? (entry.getTargetId() != null ? List.of(entry.getTargetId()) : List.of())
                : entry.getTargetIds();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) continue;

            boolean controlApplied = creatureControlService.applyControlEffect(gameData, entry.getControllerId(), target,
                    e, e.duration(), null, FaceDownVisibility.sourceName(gameData, entry));

            if (controlApplied && e.grantedSubtype() != null) {
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), FaceDownVisibility.sourceName(gameData, entry), entry.getSourcePermanentId(),
                        entry.getControllerId(), new GrantSubtypeEffect(e.grantedSubtype(), Subjects.target()),
                        targetId, null, null, Duration.PERMANENT, 0));

                gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" becomes a " + e.grantedSubtype().getDisplayName() + " in addition to its other types.").build());
            }
        }
    }

    private void resolveTemporary(GameData gameData, StackEntry entry, GainControlOfTargetEffect e) {
        List<UUID> targetIds = !isTargeted(e) ? named(gameData, entry, e) : entry.targetsForEffect(e);
        if (targetIds.isEmpty() && entry.getTargetId() != null && isTargeted(e)) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) continue;

            boolean controlApplied = creatureControlService.applyControlEffect(gameData, entry.getControllerId(), target,
                    e, e.duration(), null, FaceDownVisibility.sourceName(gameData, entry));

            // "When you lose control of the creature, tap it" (Ray of Command, Magus of the Unseen): a delayed
            // triggered ability about the stolen permanent, with no stated duration (CR 603.7b, 603.7c).
            if (controlApplied && e.tapWhenControlLost()) {
                gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                        entry.getControllerId(), entry.getCard(),
                        new TriggeredAbility(Triggers.youLoseControlOfThis(),
                                new TapPermanentsEffect(Subjects.triggering())),
                        target.getId(), entry.getSourcePermanentId(), false));
            }
        }
    }

    private void resolveWhileSource(GameData gameData, StackEntry entry, GainControlOfTargetEffect e,
                                    boolean requireSourceController, boolean requireSourceTapped) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability has no effect (source left the battlefield)."));
            return;
        }
        Permanent sourceSnapshot = entry.getSourcePermanentSnapshot();
        if (requireSourceController) {
            UUID sourceController = gameQueryService.findPermanentController(gameData, sourcePermanentId);
            if (sourceController == null || !sourceController.equals(entry.getControllerId())
                    || sourceSnapshot != null
                    && source.getControlChangeSequence() != sourceSnapshot.getControlChangeSequence()) {
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        "'s ability has no effect (controller no longer controls " + FaceDownVisibility.publicName(gameData, source) + ")."));
                return;
            }
        }
        if (requireSourceTapped && (!source.isTapped() || sourceSnapshot != null
                && source.getUntapSequence() != sourceSnapshot.getUntapSequence())) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability has no effect (" + FaceDownVisibility.publicName(gameData, source) + " is no longer tapped)."));
            return;
        }

        List<UUID> targetIds = !isTargeted(e) ? named(gameData, entry, e) : entry.targetsForEffect(e);
        if (targetIds.isEmpty() && entry.getTargetId() != null && isTargeted(e)) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) continue;

            creatureControlService.applyControlEffect(gameData, entry.getControllerId(), target,
                    e, e.duration(), sourcePermanentId, FaceDownVisibility.sourceName(gameData, entry));
        }
    }

    private static boolean isTargeted(GainControlOfTargetEffect e) {
        return e.subject() instanceof PermanentSubject.Target;
    }

    /** "That permanent", "that creature": the permanents the effect's subject names, none of them targeted. */
    private List<UUID> named(GameData gameData, StackEntry entry, GainControlOfTargetEffect e) {
        return subjectResolutionService.permanents(gameData, entry, e, e.subject()).stream()
                .map(Permanent::getId)
                .toList();
    }
}
