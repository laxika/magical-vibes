package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyTargetPermanentEffect}. A {@code target()} subject destroys the effect's target (every bound
 * target group at once when the effect is bound to several) and may create a token for its controller; any other
 * subject is found by {@link SubjectResolutionService} and its permanents are destroyed as one batch through
 * {@link DestructionSupport#destroyBatch}, which owns indestructible, regeneration and the death replacements. A
 * permanent a non-target subject names that is gone at resolution is simply not destroyed.
 */
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyTargetPermanentEffect) effect;
        entry.recordStep(StepKind.DESTROY, StepOutcome.NONE);
        if (!(destroy.subject() instanceof PermanentSubject.Target)) {
            destroyNamedPermanents(gameData, entry, destroy);
            return;
        }
        if (destroy.tokenForController() == null && destroyEveryBoundGroupSimultaneously(gameData, entry, destroy)) {
            return;
        }
        List<UUID> effectTargets = entry.targetsForEffect(effect);
        UUID targetId = destroy.targetGroup() >= 0
                ? entry.targetsForGroup(destroy.targetGroup()).stream().findFirst().orElse(null)
                : effectTargets.isEmpty() ? entry.getTargetId() : effectTargets.getFirst();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        // Capture the controller before destruction (needed for token creation)
        UUID controllerId = rememberTarget(gameData, entry, target);
        if (destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(),
                destroy.cannotBeRegenerated())) {
            entry.recordStep(StepKind.DESTROY, StepOutcome.ofObjects(List.of(target.getId()),
                    controllerId == null ? List.of() : List.of(controllerId)));
        }

        // Create token for the target's controller if specified
        if (destroy.tokenForController() != null && controllerId != null
                && (!destroy.tokenOnlyForSourceController()
                || controllerId.equals(entry.getControllerId()))) {
            destructionSupport.createTokenForPlayer(gameData, controllerId, destroy.tokenForController(),
                    destroy.tokenCount(), entry.getCard().getName(), entry.getCard().getSetCode());
        }
    }

    /**
     * Destroys every permanent a subject other than a plain target names, as one event. The targeted part of an
     * {@code allOf} subject keeps the target's last-known controller and power for later instructions; for a
     * triggering subject, "that permanent's controller" later reads the controller it had as it was destroyed.
     */
    private void destroyNamedPermanents(GameData gameData, StackEntry entry, DestroyTargetPermanentEffect destroy) {
        List<Permanent> named = subjectResolutionService.permanents(gameData, entry, destroy, destroy.subject());
        if (named.isEmpty()) {
            return;
        }
        Set<UUID> targeted = new HashSet<>();
        if (destroy.subject() instanceof PermanentSubject.AllOf allOf) {
            for (PermanentSubject part : allOf.subjects()) {
                if (part.declaresTarget()) {
                    targeted.addAll(subjectResolutionService.permanentIds(gameData, entry, destroy, part));
                }
            }
        }
        for (Permanent permanent : named) {
            if (targeted.contains(permanent.getId())) {
                rememberTarget(gameData, entry, permanent);
            }
        }
        if (destroy.subject() instanceof PermanentSubject.Triggering) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, named.getFirst().getId());
            if (controllerId != null) {
                entry.setTriggeringPermanentControllerId(controllerId);
            }
        }
        destroyAndRecord(gameData, entry, named, destroy.cannotBeRegenerated());
    }

    /**
     * Destroys {@code permanents} as one batch and records the step's result: those actually destroyed (wherever they
     * went; indestructible and regenerated ones were not) and their controllers as they were destroyed (CR 608.2h).
     */
    private void destroyAndRecord(GameData gameData, StackEntry entry, List<Permanent> permanents,
                                  boolean cannotBeRegenerated) {
        Map<UUID, UUID> controllers = new HashMap<>();
        for (Permanent permanent : permanents) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (controllerId != null) {
                controllers.put(permanent.getId(), controllerId);
            }
        }
        List<UUID> destroyed = destructionSupport.destroyBatchDestroyed(gameData, permanents,
                entry.getCard().getName(), cannotBeRegenerated).stream().map(Permanent::getId).toList();
        entry.recordStep(StepKind.DESTROY, StepOutcome.ofObjects(destroyed, destroyed.stream()
                .map(controllers::get).filter(Objects::nonNull).toList()));
    }

    /** Records {@code target}'s controller, card and power as it is destroyed; returns that controller. */
    private UUID rememberTarget(GameData gameData, StackEntry entry, Permanent target) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId != null) {
            entry.getRemovedPermanentControllers().put(target.getId(), controllerId);
        }
        entry.rememberLastKnownPermanentCard(target.getId(), target.getCard());
        entry.getLastKnownPermanentPowers().put(target.getId(),
                gameQueryService.getEffectivePower(gameData, target));
        return controllerId;
    }

    /**
     * One destroy effect bound to several target groups ("destroy target artifact and target
     * enchantment") destroys all of its targets at the same time, so each dying permanent's leaves-
     * the-battlefield abilities see the others die with it. Returns {@code false} when the effect is
     * bound to at most one group.
     */
    private boolean destroyEveryBoundGroupSimultaneously(GameData gameData, StackEntry entry,
                                                          DestroyTargetPermanentEffect destroy) {
        Card targeting = entry.getTargetingCard();
        if (targeting == null) {
            return false;
        }
        Set<Integer> groups = new LinkedHashSet<>();
        for (int occurrence = 0; targeting.getEffectTargetIndex(destroy, occurrence) >= 0; occurrence++) {
            groups.add(targeting.getEffectTargetIndex(destroy, occurrence));
        }
        if (groups.size() < 2) {
            return false;
        }
        Set<UUID> targetIds = new LinkedHashSet<>();
        for (int group : groups) {
            targetIds.addAll(entry.targetsForGroup(group));
        }
        List<Permanent> toDestroy = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            rememberTarget(gameData, entry, target);
            toDestroy.add(target);
        }
        destroyAndRecord(gameData, entry, toDestroy, destroy.cannotBeRegenerated());
        return true;
    }
}
