package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.UUID;

/**
 * Puts {@code effect} onto the stack as a reflexive triggered ability.
 *
 * <p>This is used as a resolution continuation after an asynchronous action such as surveil has
 * completed. The wrapped effect is not targeted until its own triggered ability resolves.</p>
 *
 * @param effect the effect of the reflexive triggered ability
 * @param useEventValueAsX whether the preceding event value supplies X for the reflexive ability
 * @param sourcePermanentId when non-null, the permanent that is the source of the reflexive ability
 *                          instead of the permanent whose ability queued it ("When a creature is put
 *                          onto the battlefield this way, it deals damage ...")
 */
public record QueueReflexiveAbilityEffect(CardEffect effect, boolean optionalTarget,
                                           boolean useEventValueAsX,
                                           boolean useEntryTargetAsTriggeringPermanent,
                                           UUID sourcePermanentId)
        implements CombatDamageTriggerContextEffect, TriggeringSpellManaValueEffect {

    public QueueReflexiveAbilityEffect(CardEffect effect) {
        this(effect, false, false, false);
    }

    public QueueReflexiveAbilityEffect(CardEffect effect, boolean optionalTarget) {
        this(effect, optionalTarget, false, false);
    }

    public QueueReflexiveAbilityEffect(CardEffect effect, boolean optionalTarget,
                                       boolean useEventValueAsX) {
        this(effect, optionalTarget, useEventValueAsX, false);
    }

    public QueueReflexiveAbilityEffect(CardEffect effect, boolean optionalTarget,
                                       boolean useEventValueAsX,
                                       boolean useEntryTargetAsTriggeringPermanent) {
        this(effect, optionalTarget, useEventValueAsX, useEntryTargetAsTriggeringPermanent, null);
    }

    public QueueReflexiveAbilityEffect(CardEffect effect, UUID sourcePermanentId) {
        this(effect, false, false, false, sourcePermanentId);
    }

    public QueueReflexiveAbilityEffect {
        Objects.requireNonNull(effect, "effect");
    }

    @Override
    public boolean targetsChosenByReflexiveAbility() {
        return true;
    }

    @Override
    public CardEffect snapshotTriggeringSpellManaValue(int manaValue) {
        CardEffect snapshot = effect instanceof TriggeringSpellManaValueEffect manaAware
                ? manaAware.snapshotTriggeringSpellManaValue(manaValue)
                : effect;
        return new QueueReflexiveAbilityEffect(snapshot, optionalTarget, useEventValueAsX,
                useEntryTargetAsTriggeringPermanent, sourcePermanentId);
    }

    /** A combat-damage trigger needs the context of the reflexive effect it queues ("that player"). */
    @Override
    public TriggerContext combatDamageTriggerContext() {
        return effect instanceof CombatDamageTriggerContextEffect contextEffect
                ? contextEffect.combatDamageTriggerContext()
                : null;
    }
}
