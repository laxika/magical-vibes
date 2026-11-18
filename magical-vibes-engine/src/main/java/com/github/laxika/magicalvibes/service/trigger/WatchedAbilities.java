package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import java.util.ArrayList;
import java.util.List;

/**
 * Finds triggered abilities by a watched trigger (a condition whose {@link Trigger#watchKey()} is it): a card's printed
 * abilities, the abilities granted to a permanent for a duration and those of any list (a graveyard card's own and
 * perpetual ones, the abilities a static ability grants). The matchers that read them judge their options and filters.
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

    /**
     * The abilities with a trigger condition watched as {@code watched} that resolved effects granted {@code permanent}
     * (the {@code GrantTriggeredAbilityEffect}s on it) or that its card perpetually gained, read from the effects
     * themselves rather than the layered pass.
     */
    public static List<TriggeredAbility> grantedByEffects(GameData gameData, Permanent permanent, Trigger watched) {
        return withWatchedTrigger(grantedByEffects(gameData, permanent), watched);
    }

    /**
     * The triggered abilities resolved effects granted {@code permanent} (the {@code GrantTriggeredAbilityEffect}s on
     * it) and those its card perpetually gained, read from the effects themselves rather than the layered pass.
     */
    public static List<TriggeredAbility> grantedByEffects(GameData gameData, Permanent permanent) {
        List<TriggeredAbility> granted = new ArrayList<>();
        // A face-down permanent has none of its card's perpetual abilities (CR 708.2a).
        if (permanent.getOriginalCard() != null && !permanent.isFaceDown()) {
            for (Ability ability : gameData.perpetualAbilities(permanent.getOriginalCard().getId())) {
                if (ability instanceof TriggeredAbility triggered) {
                    granted.add(triggered);
                }
            }
        }
        for (FloatingContinuousEffect fe : gameData.floatingEffectsOnPermanent(permanent.getId(),
                GrantTriggeredAbilityEffect.class)) {
            if (((GrantTriggeredAbilityEffect) fe.effect()).grantedAbility() instanceof TriggeredAbility triggered) {
                granted.add(triggered);
            }
        }
        return granted;
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
