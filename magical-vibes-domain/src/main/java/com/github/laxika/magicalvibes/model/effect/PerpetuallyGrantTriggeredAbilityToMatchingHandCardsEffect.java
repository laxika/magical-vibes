package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/** Perpetually grants triggered effects to every matching card currently in the controller's hand. */
public record PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect(
        List<Ability> grantedAbilities,
        CardPredicate filter) implements CardEffect {

    /** One triggered ability with {@code trigger} for each of {@code grantedEffects}, in order. */
    public PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect(Trigger trigger,
                                                                     List<CardEffect> grantedEffects,
                                                                     CardPredicate filter) {
        this(grantedEffects.stream().<Ability>map(effect -> new TriggeredAbility(trigger, effect)).toList(), filter);
    }

    /** The effects of {@link #grantedAbilities}, in order. */
    public List<CardEffect> grantedEffects() {
        return grantedAbilities.stream().flatMap(ability -> AbilityEffects.effectsOf(ability).stream()).toList();
    }
}
