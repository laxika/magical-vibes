package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Trigger;

import java.util.List;
import java.util.Objects;

/** Chooses a matching card in the controller's hand and perpetually grants it a triggered ability. */
public record ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect(
        List<Ability> grantedAbilities,
        CardPredicate cardFilter) implements CardEffect {

    /** One triggered ability with {@code trigger} for each of {@code grantedEffects}, in order. */
    public ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect(Trigger trigger,
                                                                      List<CardEffect> grantedEffects,
                                                                      CardPredicate cardFilter) {
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(cardFilter, "cardFilter");
        this(grantedEffects.stream().<Ability>map(effect -> new TriggeredAbility(trigger, effect)).toList(),
                cardFilter);
    }

    /** The effects of {@link #grantedAbilities}, in order. */
    public List<CardEffect> grantedEffects() {
        return grantedAbilities.stream().flatMap(ability -> AbilityEffects.effectsOf(ability).stream()).toList();
    }
}
