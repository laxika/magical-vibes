package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the triggered abilities a {@link TriggerWatch}'s collectors read, by the watched trigger (a condition whose
 * {@link Trigger#watchKey()} is it): a card's printed abilities, the abilities granted to a permanent for a duration
 * and those of any list (a graveyard card's own and perpetual ones, the abilities a static ability grants). The
 * family readers ({@link CombatAbilityReader}, {@link DamageAbilityReader}, {@link CounterAbilityReader},
 * {@link TargetAbilityReader}) read the options and filters of what these find.
 */
public final class WatchedAbilities {

    private WatchedAbilities() {
    }

    /**
     * The abilities of {@code card} with a trigger condition whose {@link Trigger#watchKey()} is {@code watched} and
     * that functions in {@code zone}, in the order they were added.
     */
    public static List<TriggeredAbility> printed(Card card, Trigger watched, Zone zone) {
        return withWatchedTrigger(card.getTriggeredAbilities(), watched, zone);
    }

    /** The abilities of {@code card} that {@code watch} reads, as {@link #printed(Card, Trigger, Zone)} says. */
    public static List<TriggeredAbility> printed(Card card, TriggerWatch watch) {
        return printed(card, watch.trigger(), watch.zone());
    }

    /**
     * The abilities with a trigger condition watched as {@code watched} granted to {@code permanent} until end of
     * turn, end of combat or next upkeep.
     */
    public static List<TriggeredAbility> temporary(Permanent permanent, Trigger watched) {
        return withWatchedTrigger(permanent.getTemporaryTriggeredAbilities(), watched);
    }

    /** The abilities with a trigger condition watched as {@code watched} granted to {@code permanent} until it leaves. */
    public static List<TriggeredAbility> persistent(Permanent permanent, Trigger watched) {
        return withWatchedTrigger(permanent.getPersistentTriggeredAbilities(), watched);
    }

    /** The abilities of {@code abilities} with a trigger condition watched as {@code watched}, in order. */
    public static List<TriggeredAbility> withWatchedTrigger(List<TriggeredAbility> abilities, Trigger watched) {
        List<TriggeredAbility> watchedAbilities = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            if (ability.hasWatchedTrigger(watched)) {
                watchedAbilities.add(ability);
            }
        }
        return watchedAbilities;
    }

    /**
     * The abilities of {@code abilities} with a trigger condition watched as {@code watched} that functions in
     * {@code zone}, in order: a graveyard card's own and perpetual abilities.
     */
    public static List<TriggeredAbility> withWatchedTrigger(List<TriggeredAbility> abilities, Trigger watched,
                                                            Zone zone) {
        List<TriggeredAbility> watchedAbilities = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            if (ability.hasWatchedTrigger(watched, zone)) {
                watchedAbilities.add(ability);
            }
        }
        return watchedAbilities;
    }

    /**
     * The effects of {@code abilities} as they are written, without their options: for code that only inspects which
     * effects an ability has (the AI's threat estimates, bushido's static bonus).
     */
    public static List<CardEffect> rawEffects(List<TriggeredAbility> abilities) {
        List<CardEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            ability.effects().forEach(registration -> effects.add(registration.effect()));
        }
        return effects;
    }
}
