package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Watcher trigger-condition record for {@code Triggers.creatureDealsDamageToYouOrYourPermanent()}:
 * whenever a creature whose colors include the source permanent's chosen color deals damage to the
 * source's controller, or to a permanent they control matching {@code damagedPermanentFilter}, the
 * source permanent deals that much damage back to that creature.
 *
 * <p>The chosen color is read from the watcher's {@code Permanent.getChosenColor()} — pair this with
 * {@link ChooseColorOnEnterEffect}. The collector puts {@link #triggeredEffect()} on the stack with the
 * damaging creature as its non-chosen target and the damage dealt as its event value. Used by Mangara's Equity.
 *
 * @param damagedPermanentFilter which permanents the controller controls count as "or a … you
 *                               control"; damage to the controller themself always qualifies
 */
public record ReflectDamageToChosenColorCreatureEffect(PermanentPredicate damagedPermanentFilter)
        implements CardEffect {

    /** "That much damage to that creature", read from the stack entry. */
    public CardEffect triggeredEffect() {
        return new DealDamageToTargetCreatureEffect(new EventValue());
    }
}
