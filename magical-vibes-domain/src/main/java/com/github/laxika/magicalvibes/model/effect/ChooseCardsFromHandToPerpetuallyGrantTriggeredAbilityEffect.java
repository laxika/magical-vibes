package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.Objects;

/** Lets the controller choose up to a bounded number of matching hand cards for a perpetual trigger grant. */
public record ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect(
        int maxCount,
        List<Ability> grantedAbilities,
        CardPredicate cardFilter)
        implements CardEffect {

    /** The grant as the slot and effects that stand for it. */
    public ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect(int maxCount,
                                                                       EffectSlot slot,
                                                                       List<CardEffect> grantedEffects,
                                                                       CardPredicate cardFilter) {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
        Objects.requireNonNull(slot, "slot");
        grantedEffects = List.copyOf(grantedEffects);
        Objects.requireNonNull(cardFilter, "cardFilter");
        this(maxCount, LegacySlots.abilities(slot, grantedEffects), cardFilter);
    }

    /** The slot of {@link #grantedAbilities}. */
    public EffectSlot slot() {
        return LegacySlots.slotOf(grantedAbilities.getFirst());
    }

    /** The effects of {@link #grantedAbilities}, in order. */
    public List<CardEffect> grantedEffects() {
        return grantedAbilities.stream().flatMap(ability -> LegacySlots.effectsOf(ability).stream()).toList();
    }
}
