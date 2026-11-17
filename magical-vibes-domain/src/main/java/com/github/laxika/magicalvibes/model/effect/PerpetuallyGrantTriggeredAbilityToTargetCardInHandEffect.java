package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/** Lets the controller choose a card in the target player's hand to receive persistent triggers. */
public record PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect(
        List<Ability> grantedAbilities,
        CardPredicate filter) implements CardEffect {

    /** One triggered ability with {@code trigger} and {@code grantedEffects}, in order (CR 603.3: one object). */
    public PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect(Trigger trigger,
                                                                    List<CardEffect> grantedEffects,
                                                                    CardPredicate filter) {
        this(List.of(new TriggeredAbility(trigger, grantedEffects.toArray(CardEffect[]::new))), filter);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    /** The effects of {@link #grantedAbilities}, in order. */
    public List<CardEffect> grantedEffects() {
        return grantedAbilities.stream().flatMap(ability -> AbilityEffects.effectsOf(ability).stream()).toList();
    }
}
