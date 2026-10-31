package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.Objects;

/** Chooses a matching card in the controller's hand and perpetually grants it a triggered ability. */
public record ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect(
        List<Ability> grantedAbilities,
        CardPredicate cardFilter) implements CardEffect {

    /** The grant as the slot and effects that stand for it. */
    public ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect(EffectSlot slot,
                                                                      List<CardEffect> grantedEffects,
                                                                      CardPredicate cardFilter) {
        Objects.requireNonNull(slot, "slot");
        grantedEffects = List.copyOf(grantedEffects);
        Objects.requireNonNull(cardFilter, "cardFilter");
        this(LegacySlots.abilities(slot, grantedEffects), cardFilter);
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
