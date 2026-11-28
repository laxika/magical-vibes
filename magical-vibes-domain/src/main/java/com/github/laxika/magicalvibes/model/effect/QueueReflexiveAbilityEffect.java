package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

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
 * @param eventValue           "that much", "where X is the number of creatures tapped this way": the reflexive
 *                             ability's event value, evaluated against the resolving entry as the ability is made
 *                             (an {@code AmountThisWay} reads the steps before it, CR 608.2h), so its target
 *                             restriction and its effect read it as the event value; {@code null} keeps the resolving
 *                             entry's
 * @param declaredTarget       "you may pay ... equal to target permanent's mana value. When you do, destroy that
 *                             permanent": the target the ability carrying this step declares as it goes on the stack
 *                             (CR 603.3d), the reflexive ability being about it with no target of its own (its
 *                             "that creature"), or {@code null} when the card-level target belongs to the reflexive
 *                             ability
 */
public record QueueReflexiveAbilityEffect(CardEffect effect, boolean useEntryTargetAsTriggeringPermanent,
                                           boolean aboutPickedPermanent, DynamicAmount eventValue,
                                           TargetSpec declaredTarget)
        implements CardEffect {

    public QueueReflexiveAbilityEffect(CardEffect effect) {
        this(effect, false, false);
    }

    public QueueReflexiveAbilityEffect(CardEffect effect, boolean useEntryTargetAsTriggeringPermanent,
                                       boolean aboutPickedPermanent, DynamicAmount eventValue) {
        this(effect, useEntryTargetAsTriggeringPermanent, aboutPickedPermanent, eventValue, null);
    }

    public QueueReflexiveAbilityEffect(CardEffect effect, boolean useEntryTargetAsTriggeringPermanent,
                                       boolean aboutPickedPermanent) {
        this(effect, useEntryTargetAsTriggeringPermanent, aboutPickedPermanent, null);
    }

    /**
     * The reflexive ability of {@code effect} whose "that much" is {@code amount} ("tap any number of untapped
     * artifacts you control. When you do, this Saga deals that much damage ...", Yotia Declares War).
     */
    public static QueueReflexiveAbilityEffect withEventValue(CardEffect effect, DynamicAmount amount) {
        return new QueueReflexiveAbilityEffect(effect, false, false, Objects.requireNonNull(amount, "amount"));
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

    /**
     * The reflexive ability about the target its carrying ability declares ({@code declaredTarget}, chosen as that
     * ability goes on the stack): it has no target, and "that permanent" is the carrying ability's target, which its
     * effect names as {@code Subjects.triggering()} (Behemoth of Vault 0: "you may pay an amount of {E} equal to target
     * nonland permanent's mana value. When you do, destroy that permanent", its rulings).
     */
    public static QueueReflexiveAbilityEffect aboutDeclaredTarget(CardEffect effect, TargetSpec declaredTarget) {
        return new QueueReflexiveAbilityEffect(effect, true, false, null,
                Objects.requireNonNull(declaredTarget, "declaredTarget"));
    }

    /** The reflexive ability about the next permanent an earlier step of the resolution picked. */
    public static QueueReflexiveAbilityEffect aboutPickedPermanent(CardEffect effect) {
        return new QueueReflexiveAbilityEffect(effect, false, true);
    }

    public QueueReflexiveAbilityEffect {
        Objects.requireNonNull(effect, "effect");
    }

    @Override
    public TargetSpec targetSpec() {
        return declaredTarget == null ? TargetSpec.NONE : declaredTarget;
    }

    @Override
    public boolean targetsChosenByReflexiveAbility() {
        return declaredTarget == null;
    }
}
