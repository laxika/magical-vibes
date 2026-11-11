package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.UUID;

/**
 * Puts {@code effect} onto the stack as a reflexive triggered ability.
 *
 * <p>This is used as a resolution continuation after an asynchronous action such as surveil has
 * completed. The wrapped effect is not targeted until its own triggered ability resolves.</p>
 *
 * <p>The reflexive ability records what the resolving ability was about: its X, its trigger event and the value the
 * preceding action recorded as the event value. An effect that means "that much" or "that many" reads the event value
 * ({@code EventValue}, an event-value predicate); X is the resolving ability's own X.</p>
 *
 * @param effect the effect of the reflexive triggered ability
 * @param optionalTarget whether the reflexive ability's controller may be chosen in place of a target
 * @param useEntryTargetAsTriggeringPermanent whether the resolving entry's target is "that creature" of the
 *                                            reflexive ability
 * @param sourcePermanentId when non-null, the permanent that is the source of the reflexive ability
 *                          instead of the permanent whose ability queued it ("When a creature is put
 *                          onto the battlefield this way, it deals damage ...")
 */
public record QueueReflexiveAbilityEffect(CardEffect effect, boolean optionalTarget,
                                           boolean useEntryTargetAsTriggeringPermanent,
                                           UUID sourcePermanentId)
        implements CardEffect {

    public QueueReflexiveAbilityEffect(CardEffect effect) {
        this(effect, false, false, null);
    }

    public QueueReflexiveAbilityEffect(CardEffect effect, boolean optionalTarget) {
        this(effect, optionalTarget, false, null);
    }

    public QueueReflexiveAbilityEffect(CardEffect effect, UUID sourcePermanentId) {
        this(effect, false, false, sourcePermanentId);
    }

    /** The reflexive ability whose "that creature" is the resolving entry's target. */
    public static QueueReflexiveAbilityEffect aboutEntryTarget(CardEffect effect) {
        return new QueueReflexiveAbilityEffect(effect, false, true, null);
    }

    public QueueReflexiveAbilityEffect {
        Objects.requireNonNull(effect, "effect");
    }

    @Override
    public boolean targetsChosenByReflexiveAbility() {
        return true;
    }
}
