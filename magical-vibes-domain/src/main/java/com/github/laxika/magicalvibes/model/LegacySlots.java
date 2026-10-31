package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Converts between abilities and the {@link EffectSlot}s that still name them. Abilities granted
 * to permanents, spells and cards at runtime are stored as abilities, while the code that grants
 * and reads them still speaks in slots; this is the one place that maps the two. It goes away with
 * {@code EffectSlot}.
 */
public final class LegacySlots {

    private LegacySlots() {
    }

    /**
     * The ability that {@code effect} in {@code slot} stands for: a {@link SpellAbility} for
     * {@code SPELL}, a {@link StaticAbility} of the battlefield or the command zone for {@code STATIC}
     * and {@code COMMAND_ZONE_STATIC}, and otherwise a {@link TriggeredAbility} with a
     * {@link LegacySlotTrigger}.
     */
    public static Ability ability(EffectSlot slot, CardEffect effect) {
        return switch (slot) {
            case SPELL -> new SpellAbility(List.of(new EffectRegistration(effect)));
            case STATIC -> new StaticAbility(List.of(effect));
            case COMMAND_ZONE_STATIC -> new StaticAbility(List.of(effect), AbilityZones.COMMAND);
            default -> triggered(slot, effect);
        };
    }

    /**
     * Like {@link #ability}, but {@code null} when {@code slot} or {@code effect} is, for a grant
     * whose ability is optional.
     */
    public static Ability grant(EffectSlot slot, CardEffect effect) {
        return slot == null || effect == null ? null : ability(slot, effect);
    }

    /** One ability for each of {@code effects} in {@code slot}, in order. */
    public static List<Ability> abilities(EffectSlot slot, List<? extends CardEffect> effects) {
        List<Ability> abilities = new ArrayList<>(effects.size());
        for (CardEffect effect : effects) {
            abilities.add(ability(slot, effect));
        }
        return List.copyOf(abilities);
    }

    /** One ability for each effect of each slot of {@code effectsBySlot}, in the map's order. */
    public static List<Ability> abilities(Map<EffectSlot, ? extends List<? extends CardEffect>> effectsBySlot) {
        List<Ability> abilities = new ArrayList<>();
        effectsBySlot.forEach((slot, effects) -> abilities.addAll(abilities(slot, effects)));
        return List.copyOf(abilities);
    }

    /**
     * The abilities that the registrations of each slot of {@code registrationsBySlot} stand for, in the
     * map's order: one for each registration, keeping its trigger mode, except that a registration marked
     * simultaneous joins the ability of the one before it. Static effects are each their own static
     * ability, without a trigger mode.
     */
    public static List<Ability> registrationAbilities(Map<EffectSlot, List<EffectRegistration>> registrationsBySlot) {
        List<Ability> abilities = new ArrayList<>();
        registrationsBySlot.forEach((slot, registrations) -> {
            if (slot == EffectSlot.STATIC || slot == EffectSlot.COMMAND_ZONE_STATIC) {
                registrations.forEach(registration -> abilities.add(ability(slot, registration.effect())));
                return;
            }
            List<EffectRegistration> group = new ArrayList<>();
            for (EffectRegistration registration : registrations) {
                if (!registration.simultaneousWithPrevious() && !group.isEmpty()) {
                    abilities.add(ability(slot, group));
                    group = new ArrayList<>();
                }
                group.add(registration);
            }
            if (!group.isEmpty()) {
                abilities.add(ability(slot, group));
            }
        });
        return List.copyOf(abilities);
    }

    private static Ability ability(EffectSlot slot, List<EffectRegistration> group) {
        return switch (slot) {
            case SPELL -> new SpellAbility(group.stream()
                    .map(registration -> new EffectRegistration(registration.effect(), TriggerMode.NORMAL,
                            registration.simultaneousWithPrevious()))
                    .toList());
            default -> new TriggeredAbility(new LegacySlotTrigger(slot), TriggerOptions.DEFAULT, group);
        };
    }

    /** The triggered ability that {@code effect} in the trigger slot {@code slot} stands for. */
    public static TriggeredAbility triggered(EffectSlot slot, CardEffect effect) {
        return new TriggeredAbility(new LegacySlotTrigger(slot), TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(effect)));
    }

    /**
     * The slot whose views show {@code ability}: {@code SPELL}, {@code STATIC} or
     * {@code COMMAND_ZONE_STATIC}, or a triggered ability's legacy slot.
     */
    public static EffectSlot slotOf(Ability ability) {
        return switch (ability) {
            case SpellAbility spell -> EffectSlot.SPELL;
            case StaticAbility statik -> statik.zones().functionsIn(Zone.COMMAND)
                    ? EffectSlot.COMMAND_ZONE_STATIC
                    : EffectSlot.STATIC;
            case TriggeredAbility triggered -> switch (triggered.conditions().getFirst().trigger()) {
                case LegacySlotTrigger legacy -> legacy.slot();
            };
            case ActivatedAbility activated -> throw new IllegalArgumentException(
                    "Activated abilities aren't kept in a slot");
        };
    }

    /** The effects of {@code ability}, in order. */
    public static List<CardEffect> effectsOf(Ability ability) {
        return switch (ability) {
            case SpellAbility spell -> registrationEffects(spell.effects());
            case TriggeredAbility triggered -> registrationEffects(triggered.effects());
            case StaticAbility statik -> statik.effects();
            case ActivatedAbility activated -> activated.getEffects();
        };
    }

    /** The effects of the abilities in {@code slot}, in order; empty when there are none. */
    public static List<CardEffect> effectsIn(Collection<? extends Ability> abilities, EffectSlot slot) {
        if (abilities.isEmpty()) {
            return List.of();
        }
        List<CardEffect> effects = null;
        for (Ability ability : abilities) {
            if (slotOf(ability) == slot) {
                if (effects == null) {
                    effects = new ArrayList<>();
                }
                effects.addAll(effectsOf(ability));
            }
        }
        return effects == null ? List.of() : List.copyOf(effects);
    }

    private static List<CardEffect> registrationEffects(List<EffectRegistration> registrations) {
        if (registrations.size() == 1) {
            return List.of(registrations.getFirst().effect());
        }
        return registrations.stream().map(EffectRegistration::effect).toList();
    }
}
