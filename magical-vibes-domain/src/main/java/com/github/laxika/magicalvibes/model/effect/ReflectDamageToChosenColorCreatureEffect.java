package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Watcher marker for {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT}:
 * whenever a creature whose colors include the source permanent's chosen color deals damage to the
 * source's controller, or to a permanent they control matching {@code damagedPermanentFilter}, the
 * source permanent deals that much damage back to that creature.
 *
 * <p>The chosen color is read from the watcher's {@code Permanent.getChosenColor()} — pair this with
 * {@link ChooseColorOnEnterEffect}. Resolution reuses {@link DealDamageToTargetCreatureEffect} with
 * the damaging creature baked in as a non-chosen target. Used by Mangara's Equity.
 *
 * @param damagedPermanentFilter which permanents the controller controls count as "or a … you
 *                               control"; damage to the controller themself always qualifies
 */
public record ReflectDamageToChosenColorCreatureEffect(PermanentPredicate damagedPermanentFilter)
        implements CardEffect {
}
