package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.Objects;

/** Lets the controller choose up to a bounded number of matching hand cards for a perpetual trigger grant. */
public record ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect(
        int maxCount,
        List<Ability> grantedAbilities,
        CardPredicate cardFilter)
        implements CardEffect {

    /** One triggered ability with {@code trigger} for each of {@code grantedEffects}, in order. */
    public ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect(int maxCount,
                                                                       Trigger trigger,
                                                                       List<CardEffect> grantedEffects,
                                                                       CardPredicate cardFilter) {
        this(maxCount, grantedEffects.stream().<Ability>map(effect -> new TriggeredAbility(trigger, effect)).toList(),
                cardFilter);
    }

    /** The effects of {@link #grantedAbilities}, in order. */
    public List<CardEffect> grantedEffects() {
        return grantedAbilities.stream().flatMap(ability -> AbilityEffects.effectsOf(ability).stream()).toList();
    }
}
