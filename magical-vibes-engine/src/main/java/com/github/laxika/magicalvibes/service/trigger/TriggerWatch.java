package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;

import java.util.List;

/**
 * An old slot collector that reads triggered abilities with a real trigger: a {@link SpellAbilityWatch}, a
 * {@link CombatWatch}, a {@link DamageWatch}, a {@link CounterWatch}, a {@link TargetWatch}, a {@link LibraryWatch},
 * a {@link TapWatch}, a {@link LifeWatch}, a {@link SagaWatch}, an {@link ActionWatch} or a {@link MiscWatch}. It names
 * the trigger the slot's cards were converted to and the zone the collector reads them in, and keys the collector's
 * handlers.
 */
public interface TriggerWatch {

    /** The trigger the slot's cards were converted to. */
    Trigger trigger();

    /** The zone the slot's collector reads the abilities in. */
    Zone zone();

    /** The effects of {@code card}'s abilities this watch reads, in the order they were added. */
    default List<CardEffect> effectsOf(Card card) {
        return card.getTriggerEffects(trigger(), zone());
    }

    /** The registrations of {@code card}'s abilities this watch reads, with their trigger modes. */
    default List<EffectRegistration> registrationsOf(Card card) {
        return card.getTriggerEffectRegistrations(trigger(), zone());
    }
}
