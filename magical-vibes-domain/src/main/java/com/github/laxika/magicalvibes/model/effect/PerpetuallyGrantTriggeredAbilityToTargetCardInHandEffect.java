package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/** Lets the controller choose a card in the target player's hand to receive persistent triggers. */
public record PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect(
        List<Ability> grantedAbilities,
        CardPredicate filter) implements CardEffect {

    /** The grant as the slot and effects that stand for it. */
    public PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect(EffectSlot slot,
                                                                    List<CardEffect> grantedEffects,
                                                                    CardPredicate filter) {
        grantedEffects = List.copyOf(grantedEffects);
        this(LegacySlots.abilities(slot, grantedEffects), filter);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
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
