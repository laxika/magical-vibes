package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringBoastAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringLoyaltyAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNinjutsuAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNonTapAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringOutlastAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPowerUpAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringTapAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.LandPlayTrigger;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A triggered ability (CR 113.3c): its trigger conditions, the options that apply to them and its
 * effects. Each time it triggers it is one object on the stack (CR 603.3).
 *
 * @param conditions the trigger conditions, at least one; the ability triggers when any of them is
 *                   met ("When this creature enters or attacks"). A {@link LegacySlotTrigger} is the
 *                   only condition of its ability.
 * @param options    the intervening "if", limit and multiplicity. An ability with a
 *                   {@link LegacySlotTrigger} gets these from its slot's collectors and wrapper
 *                   effects instead, so its options are {@link TriggerOptions#DEFAULT}.
 * @param effects    the effects in the order they resolve, at least one. Effects marked simultaneous
 *                   happen at the same time as the effect before them, so the first one can't be.
 * @param chapterTargets the targets of a Saga chapter ability, or {@link SagaChapterTargets#NONE}.
 *                   The targets of other triggered abilities are still bound on the {@link Card}.
 */
public record TriggeredAbility(List<TriggerCondition> conditions, TriggerOptions options,
                               List<EffectRegistration> effects, SagaChapterTargets chapterTargets)
        implements Ability {

    public TriggeredAbility {
        conditions = List.copyOf(Objects.requireNonNull(conditions, "conditions"));
        Objects.requireNonNull(options, "options");
        effects = List.copyOf(Objects.requireNonNull(effects, "effects"));
        Objects.requireNonNull(chapterTargets, "chapterTargets");
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("A triggered ability needs at least one trigger condition");
        }
        if (effects.isEmpty()) {
            throw new IllegalArgumentException("A triggered ability needs at least one effect");
        }
        if (effects.getFirst().simultaneousWithPrevious()) {
            throw new IllegalArgumentException("The first effect of a triggered ability has no effect before it");
        }
        if (conditions.stream().anyMatch(condition -> condition.trigger() instanceof LegacySlotTrigger)) {
            if (conditions.size() > 1) {
                throw new IllegalArgumentException("A legacy slot trigger is the only condition of its ability");
            }
            if (!options.equals(TriggerOptions.DEFAULT)) {
                throw new IllegalArgumentException("A legacy slot trigger takes its options from its slot's "
                        + "collectors and wrapper effects, not from TriggerOptions");
            }
        }
    }

    /** An ability that isn't a Saga chapter ability. */
    public TriggeredAbility(List<TriggerCondition> conditions, TriggerOptions options,
                            List<EffectRegistration> effects) {
        this(conditions, options, effects, SagaChapterTargets.NONE);
    }

    /** An ability with one trigger condition, which functions from {@code trigger}'s default zones. */
    public TriggeredAbility(Trigger trigger, TriggerOptions options, List<EffectRegistration> effects) {
        this(List.of(new TriggerCondition(trigger)), options, effects);
    }

    /**
     * An ability with one trigger condition, which functions from {@code trigger}'s default zones, and no
     * options: "when this creature dies, draw a card" is {@code new TriggeredAbility(Triggers.dies(THIS),
     * new DrawCardEffect(1))}. The effects are one ability, in the order they resolve.
     */
    public TriggeredAbility(Trigger trigger, CardEffect... effects) {
        this(trigger, TriggerOptions.DEFAULT, Arrays.stream(effects).map(EffectRegistration::new).toList());
    }

    /**
     * An ability with one trigger condition, which functions from the zones {@code condition} names, and no
     * options: "while this card is in your graveyard, whenever a creature you control dies".
     */
    public TriggeredAbility(TriggerCondition condition, CardEffect... effects) {
        this(List.of(condition), TriggerOptions.DEFAULT,
                Arrays.stream(effects).map(EffectRegistration::new).toList());
    }

    /** Whether {@code trigger} is one of this ability's trigger conditions. */
    public boolean hasTrigger(Trigger trigger) {
        return conditions.stream().anyMatch(condition -> condition.trigger().equals(trigger));
    }

    /**
     * Whether one of this ability's trigger conditions has {@code trigger} as its {@link Trigger#watchKey()} and
     * functions in {@code zone}: the abilities a collector that still reads effects by trigger reads.
     */
    public boolean hasWatchedTrigger(Trigger trigger, Zone zone) {
        return conditions.stream().anyMatch(condition -> condition.trigger().watchKey().equals(trigger)
                && condition.zones().functionsIn(zone));
    }

    /**
     * Whether {@code trigger} is one of this ability's trigger conditions and that condition functions in
     * {@code zone}.
     */
    public boolean hasTrigger(Trigger trigger, Zone zone) {
        return conditions.stream().anyMatch(condition -> condition.trigger().equals(trigger)
                && condition.zones().functionsIn(zone));
    }

    /**
     * The key "triggers only once each turn" marks for this ability on its source, so a limit covers every trigger
     * condition of the ability and no other ability of the source.
     */
    public String limitKey() {
        return "trigger:" + Integer.toHexString(hashCode());
    }

    /**
     * This ability's registrations in the form the code that still reads effects instead of abilities takes them
     * (the "when this enters" collector and the code that inspects "when this enters" abilities, until step 15 of
     * effect-slot-refactor.md): each effect wrapped in this ability's options as the cards wrote them before step 14.
     * An intervening "if" is a {@link ConditionalEffect} of it, checked as the ability triggers and again as it
     * resolves (CR 603.4); "triggers only once each turn" is a {@link OncePerTurnTriggerEffect} keyed by
     * {@link #limitKey()}, the key the trigger pipeline marks; "do this only once each turn" one marked as the action
     * is taken (CR 603.2h); "this ability triggers only once" ({@code ONCE}) an {@link OnceOnlyTriggerEffect} inside
     * the intervening "if", as the "at the beginning of [step or phase]" cards wrote it. A land-play trigger's filter
     * is a {@link TriggeringCardConditionalEffect} inside the limit, an activation trigger's source filter a
     * {@link TriggeringPermanentConditionalEffect} there and its ability kind the matching
     * {@code Triggering…AbilityConditionalEffect} outside everything, as the spell, land-play and activation cards
     * wrote them. An ability without options or such filters returns its registrations as they are.
     */
    public List<EffectRegistration> legacyEffects() {
        Condition interveningIf = options.interveningIf();
        TriggerLimit limit = options.limit();
        LandPlayTrigger landPlay = filteredTrigger(LandPlayTrigger.class);
        AbilityTrigger activation = filteredTrigger(AbilityTrigger.class);
        if (interveningIf == null && limit == TriggerLimit.NONE && landPlay == null && activation == null) {
            return effects;
        }
        String key = limit == TriggerLimit.ONCE_EACH_TURN ? limitKey() : null;
        List<EffectRegistration> wrapped = new ArrayList<>(effects.size());
        for (EffectRegistration registration : effects) {
            CardEffect effect = registration.effect();
            if (limit == TriggerLimit.ONCE) {
                effect = new OnceOnlyTriggerEffect(effect);
            }
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect, true);
            }
            if (landPlay != null && landPlay.filter() != null) {
                effect = new TriggeringCardConditionalEffect(landPlay.filter(), effect);
            }
            if (activation != null && activation.source() != null) {
                effect = new TriggeringPermanentConditionalEffect(activation.source(), effect);
            }
            if (limit == TriggerLimit.ONCE_EACH_TURN) {
                effect = OncePerTurnTriggerEffect.keyed(effect, key);
            } else if (limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN) {
                effect = OncePerTurnTriggerEffect.markOnAcceptance(effect);
            }
            if (activation != null && activation.kind() != null) {
                effect = legacyKindWrapper(activation.kind(), effect);
            }
            wrapped.add(new EffectRegistration(effect, registration.triggerMode(),
                    registration.simultaneousWithPrevious()));
        }
        return List.copyOf(wrapped);
    }

    /**
     * The first trigger of {@code type} with filters among this ability's conditions, or {@code null} for none. The
     * legacy view wraps every effect in one set of filters, so every condition of that type must have the same
     * filters.
     */
    private <T extends Trigger> T filteredTrigger(Class<T> type) {
        List<T> ofType = conditions.stream().map(TriggerCondition::trigger).filter(type::isInstance)
                .map(type::cast).toList();
        T filtered = ofType.stream().filter(trigger -> !trigger.watchKey().equals(trigger)).findFirst().orElse(null);
        if (filtered != null && ofType.stream().anyMatch(t -> !filters(t).equals(filters(filtered)))) {
            throw new IllegalStateException("The " + type.getSimpleName()
                    + " conditions of one ability must have the same filters: " + ofType);
        }
        return filtered;
    }

    private static List<Object> filters(Trigger trigger) {
        return switch (trigger) {
            case LandPlayTrigger landPlay -> Arrays.asList(landPlay.filter());
            case AbilityTrigger activation -> Arrays.asList(activation.source(), activation.kind());
            default -> List.of();
        };
    }

    private static CardEffect legacyKindWrapper(AbilityTrigger.AbilityKind kind, CardEffect effect) {
        return switch (kind) {
            case LOYALTY -> new TriggeringLoyaltyAbilityConditionalEffect(effect);
            case NINJUTSU -> new TriggeringNinjutsuAbilityConditionalEffect(effect);
            case OUTLAST -> new TriggeringOutlastAbilityConditionalEffect(effect);
            case POWER_UP -> new TriggeringPowerUpAbilityConditionalEffect(effect);
            case BOAST -> new TriggeringBoastAbilityConditionalEffect(effect);
            case WITH_TAP_COST -> new TriggeringTapAbilityConditionalEffect(effect);
            case WITHOUT_TAP_COST -> new TriggeringNonTapAbilityConditionalEffect(effect);
        };
    }

    /** This ability with {@code effects} in place of its effects. */
    public TriggeredAbility withEffects(List<EffectRegistration> effects) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets);
    }

    /** This ability with {@code chapterTargets} in place of its Saga chapter targets. */
    public TriggeredAbility withChapterTargets(SagaChapterTargets chapterTargets) {
        return new TriggeredAbility(conditions, options, effects, chapterTargets);
    }
}
