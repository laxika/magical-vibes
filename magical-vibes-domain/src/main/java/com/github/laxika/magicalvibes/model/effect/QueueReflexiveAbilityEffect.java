package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

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
 * @param useEntryTargetAsTriggeringPermanent whether the resolving entry's target is "that creature" of the
 *                                            reflexive ability
 * @param aboutPickedPermanent whether the reflexive ability is about a permanent an earlier step of the resolution
 *                             picked ("when a creature is put onto the battlefield this way, it deals damage ...",
 *                             "create a token. When you do, ... that token"): the first of the resolving entry's picks
 *                             ({@code StackEntry.recordPicks}) is taken off them and is the source permanent of the
 *                             reflexive ability instead of the permanent whose ability queued it, so one such step
 *                             per picked permanent puts one ability on the stack for each, in order. With no pick
 *                             left the ability is not put on the stack
 */
public record QueueReflexiveAbilityEffect(CardEffect effect, boolean useEntryTargetAsTriggeringPermanent,
                                           boolean aboutPickedPermanent)
        implements CardEffect {

    public QueueReflexiveAbilityEffect(CardEffect effect) {
        this(effect, false, false);
    }

    /**
     * The reflexive ability of {@code effect}, whose target is "up to one target" when {@code upToOneTarget} (its
     * controller may choose none).
     */
    public static QueueReflexiveAbilityEffect of(CardEffect effect, boolean upToOneTarget) {
        return new QueueReflexiveAbilityEffect(upToOneTarget ? SequenceEffect.upToOneTarget(effect) : effect);
    }

    /** The reflexive ability whose "that creature" is the resolving entry's target. */
    public static QueueReflexiveAbilityEffect aboutEntryTarget(CardEffect effect) {
        return new QueueReflexiveAbilityEffect(effect, true, false);
    }

    /** The reflexive ability about the next permanent an earlier step of the resolution picked. */
    public static QueueReflexiveAbilityEffect aboutPickedPermanent(CardEffect effect) {
        return new QueueReflexiveAbilityEffect(effect, false, true);
    }

    public QueueReflexiveAbilityEffect {
        Objects.requireNonNull(effect, "effect");
    }

    @Override
    public boolean targetsChosenByReflexiveAbility() {
        return true;
    }
}
