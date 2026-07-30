package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

/**
 * "The next {@code amount} damage that would be dealt this turn to target white creature you
 * control is dealt to this creature instead." The mirror image of
 * {@link RedirectNextDamageToTargetCreatureEffect}: here the ability's target is the creature
 * being protected and the redirect destination is the ability's own source permanent. Applies to
 * the next {@code amount} damage from any source (combat or noncombat), then the shield is
 * consumed. Used by Hazduhr the Abbot.
 */
public record RedirectNextDamageToTargetCreatureToSourceEffect(DynamicAmount amount) implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        // The "you control" half rides on the ability's ControlledPermanentPredicateTargetFilter:
        // this spec is evaluated without a source permanent, so a controller predicate cannot work here.
        return TargetSpec.benign(TargetCategory.CREATURE, new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.WHITE)))));
    }
}
