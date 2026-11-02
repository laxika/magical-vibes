package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Trigger;

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

    /** One triggered ability with {@code trigger} for each of {@code grantedEffects}, in order. */
    public ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect(Trigger trigger,
                                                                      List<CardEffect> grantedEffects,
                                                                      CardPredicate cardFilter) {
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(cardFilter, "cardFilter");
        this(grantedEffects.stream().<Ability>map(effect -> new TriggeredAbility(trigger, effect)).toList(),
                cardFilter);
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
