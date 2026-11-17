package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reads and adjusts the effects of an {@link Ability} of any kind, for code that handles spell, triggered, static and
 * activated abilities alike (grants, copies, views, logs).
 */
public final class AbilityEffects {

    private AbilityEffects() {
    }

    /**
     * {@code ability} with {@code effect} as its only effect: a static ability keeps its zones and a triggered ability
     * its trigger conditions and options. Grants use it to grant an ability whose effect they adjusted.
     */
    public static Ability withEffect(Ability ability, CardEffect effect) {
        return switch (ability) {
            case StaticAbility statik -> new StaticAbility(List.of(effect), statik.zones());
            case TriggeredAbility triggered -> triggered.withEffects(List.of(new EffectRegistration(effect)));
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

    /** A name for the kind of {@code ability}, for logs: {@code TRIGGERED}, {@code STATIC}, {@code SPELL} or {@code ACTIVATED}. */
    public static String label(Ability ability) {
        return switch (ability) {
            case SpellAbility spell -> "SPELL";
            case StaticAbility statik -> "STATIC";
            case TriggeredAbility triggered -> "TRIGGERED";
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
