package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Global-watcher marker for {@code ON_ANY_SOURCE_DEALS_DAMAGE}: whenever a source matching
 * {@code sourceFilter} (creature or spell, any controller) deals damage, the permanent holding this
 * effect gains that much life for that source's controller (Essence Sliver: "Whenever a Sliver deals
 * damage, its controller gains that much life."). The watcher's controller controls the triggered
 * ability, so the life goes to the damage source's controller rather than to the watcher's controller.
 * Never resolved directly — it is read by the {@code ON_ANY_SOURCE_DEALS_DAMAGE} trigger collector,
 * which queues a {@code GainLifeEffect(EventValue, TRIGGERING_PLAYER)} triggered ability targeting
 * that controller without making it a targeted ability.
 *
 * @param sourceFilter predicate the damage source's card must satisfy
 */
public record SourceDamageGainLifeForSourceControllerEffect(CardPredicate sourceFilter) implements CardEffect {
}
