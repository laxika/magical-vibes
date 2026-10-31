package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     * The ability that {@code effect} in {@code slot} stands for: a {@link TriggeredAbility} with a
     * {@link LegacySlotTrigger}. Static and spell abilities have left the slots, so they are built
     * directly ({@link StaticAbility#of}).
     */
    public static Ability ability(EffectSlot slot, CardEffect effect) {
        return triggered(Objects.requireNonNull(slot, "slot"), effect);
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
     * simultaneous joins the ability of the one before it.
     */
    public static List<Ability> registrationAbilities(Map<EffectSlot, List<EffectRegistration>> registrationsBySlot) {
        List<Ability> abilities = new ArrayList<>();
        registrationsBySlot.forEach((slot, registrations) -> {
            List<EffectRegistration> group = new ArrayList<>();
            for (EffectRegistration registration : registrations) {
                if (!registration.simultaneousWithPrevious() && !group.isEmpty()) {
                    abilities.add(new TriggeredAbility(new LegacySlotTrigger(slot), TriggerOptions.DEFAULT, group));
                    group = new ArrayList<>();
                }
                group.add(registration);
            }
            if (!group.isEmpty()) {
                abilities.add(new TriggeredAbility(new LegacySlotTrigger(slot), TriggerOptions.DEFAULT, group));
            }
        });
        return List.copyOf(abilities);
    }

    /** The triggered ability that {@code effect} in the trigger slot {@code slot} stands for. */
    public static TriggeredAbility triggered(EffectSlot slot, CardEffect effect) {
        return new TriggeredAbility(new LegacySlotTrigger(slot), TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(effect)));
    }

    /**
     * The triggered ability that {@code effects} in the trigger slot {@code slot} stand for, each a
     * registration of its own. A card that it is added to groups them as the slot's collectors do
     * ({@code Card.addAbility}).
     */
    public static TriggeredAbility triggered(EffectSlot slot, List<? extends CardEffect> effects) {
        return new TriggeredAbility(new LegacySlotTrigger(slot), TriggerOptions.DEFAULT,
                effects.stream().map(EffectRegistration::new).toList());
    }

    /**
     * The slot whose views show {@code ability}: a triggered ability's legacy slot. Spell and static
     * abilities have left the slots, so they have none ({@code null}).
     */
    public static EffectSlot slotOf(Ability ability) {
        return switch (ability) {
            case SpellAbility spell -> null;
            case StaticAbility statik -> null;
            case TriggeredAbility triggered -> switch (triggered.conditions().getFirst().trigger()) {
                case LegacySlotTrigger legacy -> legacy.slot();
            };
            case ActivatedAbility activated -> throw new IllegalArgumentException(
                    "Activated abilities aren't kept in a slot");
        };
    }

    /**
     * {@code ability} with {@code effect} as its only effect: a static ability keeps its zones and a
     * triggered ability its slot. Grants use it to grant an ability whose effect they adjusted.
     */
    public static Ability withEffect(Ability ability, CardEffect effect) {
        return switch (ability) {
            case StaticAbility statik -> new StaticAbility(List.of(effect), statik.zones());
            case TriggeredAbility triggered -> triggered(slotOf(triggered), effect);
            case SpellAbility spell -> throw new IllegalArgumentException("A spell ability isn't granted");
            case ActivatedAbility activated -> throw new IllegalArgumentException(
                    "Activated abilities are granted as activated abilities");
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

    /** The effects of the static abilities among {@code abilities}, in order; empty when there are none. */
    public static List<CardEffect> staticEffectsIn(Collection<? extends Ability> abilities) {
        if (abilities.isEmpty()) {
            return List.of();
        }
        List<CardEffect> effects = null;
        for (Ability ability : abilities) {
            if (ability instanceof StaticAbility statik) {
                if (effects == null) {
                    effects = new ArrayList<>();
                }
                effects.addAll(statik.effects());
            }
        }
        return effects == null ? List.of() : List.copyOf(effects);
    }

    /**
     * A name for the kind of {@code ability}, for logs: its legacy slot, or {@code STATIC} or
     * {@code SPELL}.
     */
    public static String label(Ability ability) {
        return switch (ability) {
            case SpellAbility spell -> "SPELL";
            case StaticAbility statik -> "STATIC";
            case TriggeredAbility triggered -> slotOf(triggered).name();
            case ActivatedAbility activated -> "ACTIVATED";
        };
    }

    private static List<CardEffect> registrationEffects(List<EffectRegistration> registrations) {
        if (registrations.size() == 1) {
            return List.of(registrations.getFirst().effect());
        }
        return registrations.stream().map(EffectRegistration::effect).toList();
    }
}
