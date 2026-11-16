package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.effect.ResolveDelayedPermanentActionEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Registers the delayed triggered ability a resolving spell or ability creates to move permanents when a step begins
 * ("exile them at the beginning of the next end step", "sacrifice it at end of combat"; CR 603.7a). Everything one
 * resolution of one source schedules for the same step is one delayed ability, with a step for each permanent; another
 * resolution of the same source creates another one.
 */
public final class DelayedPermanentActions {

    private DelayedPermanentActions() {
    }

    /**
     * Schedules {@code action} as {@code entry}'s delayed ability, controlled by its controller, with its source
     * (CR 603.7d, 603.7e), triggering at {@code player}'s step: {@link ActivePlayer#ANY} for "the next", {@link
     * ActivePlayer#YOU} for "your next".
     */
    public static void schedule(GameData gameData, StackEntry entry, DelayedPermanentAction action,
                                ActivePlayer player) {
        schedule(gameData, entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), action, player);
    }

    /**
     * Schedules {@code action} as a delayed ability of {@code permanent}'s own, for a permanent that was put onto the
     * battlefield with the delayed move built in ("sacrifice it at the beginning of the next end step" of a token
     * Choreographed Sparks made): its source is the permanent, controlled by {@code controllerId}.
     */
    public static void scheduleOwn(GameData gameData, UUID controllerId, Permanent permanent,
                                   DelayedPermanentAction action, ActivePlayer player) {
        schedule(gameData, controllerId, permanent.getCard(), permanent.getId(), action, player);
    }

    /**
     * Schedules {@code action} for {@code permanent}: as the delayed ability of the spell or ability resolving now, or
     * whose resolution waits for the choice that is completing, when there is one (CR 603.7d), else as
     * {@code permanent}'s own, controlled by {@code controllerId}.
     */
    public static void scheduleForResolution(GameData gameData, UUID controllerId, Permanent permanent,
                                             DelayedPermanentAction action, ActivePlayer player) {
        StackEntry resolving = gameData.resolvingEntry != null
                ? gameData.resolvingEntry : gameData.pendingEffectResolutionEntry;
        if (resolving != null && resolving.getCard() != null) {
            schedule(gameData, resolving, action, player);
        } else {
            scheduleOwn(gameData, controllerId, permanent, action, player);
        }
    }

    /**
     * Schedules {@code action} as the delayed ability of the source {@code sourceCard} and {@code sourcePermanentId}
     * (or none), controlled by {@code controllerId}, triggering at {@code player}'s step.
     */
    public static void schedule(GameData gameData, UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                DelayedPermanentAction action, ActivePlayer player) {
        DelayedPermanentAction scheduled = action.scheduledBy(gameData.resolutionOrdinal);
        StepTrigger step = new StepTrigger(scheduled.kind().moment(), player);
        List<TemporaryGlobalTriggeredAbility> abilities = gameData.temporaryGlobalTriggeredAbilities;
        synchronized (abilities) {
            for (int i = 0; i < abilities.size(); i++) {
                TemporaryGlobalTriggeredAbility existing = abilities.get(i);
                if (!sharesResolution(existing, controllerId, sourceCard, sourcePermanentId, step, scheduled)) {
                    continue;
                }
                List<EffectRegistration> effects = new ArrayList<>(existing.ability().effects());
                effects.add(new EffectRegistration(new ResolveDelayedPermanentActionEffect(scheduled)));
                List<UUID> permanentIds = new ArrayList<>(existing.referents().permanentIds());
                permanentIds.add(scheduled.permanentId());
                abilities.set(i, existing
                        .withAbility(new TriggeredAbility(step, existing.ability().options(), effects))
                        .withReferents(existing.referents().withPermanentIds(permanentIds)));
                return;
            }
            abilities.add(TemporaryGlobalTriggeredAbility.atNextStep(controllerId, sourceCard,
                    new TriggeredAbility(step, new ResolveDelayedPermanentActionEffect(scheduled)),
                    sourcePermanentId, scheduled.permanentId(), null,
                    DelayedReferents.permanents(List.of(scheduled.permanentId()))));
        }
    }

    private static boolean sharesResolution(TemporaryGlobalTriggeredAbility existing, UUID controllerId,
                                            Card sourceCard, UUID sourcePermanentId, StepTrigger step,
                                            DelayedPermanentAction scheduled) {
        if (scheduled.schedulingResolution() == 0
                || !existing.controllerId().equals(controllerId)
                || !existing.sourceCard().getId().equals(sourceCard.getId())
                || !java.util.Objects.equals(existing.sourcePermanentId(), sourcePermanentId)
                || existing.ability().conditions().size() != 1
                || !existing.ability().conditions().getFirst().trigger().equals(step)
                || existing.ability().effects().isEmpty()) {
            return false;
        }
        return existing.ability().effects().stream().allMatch(registration ->
                registration.effect() instanceof ResolveDelayedPermanentActionEffect resolve
                        && resolve.action().schedulingResolution() == scheduled.schedulingResolution()
                        && resolve.action().kind() == scheduled.kind());
    }
}
