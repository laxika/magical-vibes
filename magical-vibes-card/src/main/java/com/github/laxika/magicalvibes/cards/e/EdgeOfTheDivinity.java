package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "87")
public class EdgeOfTheDivinity extends Card {

    public EdgeOfTheDivinity() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // As long as enchanted creature is white, it gets +1/+2.
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        new StaticBoostEffect(1, 2, Set.of(), GrantScope.ENCHANTED_CREATURE),
                        null
                ))
                // As long as enchanted creature is black, it gets +2/+1.
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                        new StaticBoostEffect(2, 1, Set.of(), GrantScope.ENCHANTED_CREATURE),
                        null
                ));
    }
}
