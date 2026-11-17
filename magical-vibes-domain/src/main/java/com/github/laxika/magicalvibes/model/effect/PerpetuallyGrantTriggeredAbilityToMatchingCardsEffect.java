package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Perpetually grants a triggered ability to matching permanents and cards in hand. */
public record PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect(
        CardPredicate handFilter,
        PermanentPredicate permanentFilter,
        Ability grantedAbility) implements CardEffect {

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
