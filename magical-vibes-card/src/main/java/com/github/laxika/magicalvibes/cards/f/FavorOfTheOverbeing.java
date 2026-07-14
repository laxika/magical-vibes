package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "151")
public class FavorOfTheOverbeing extends Card {

    public FavorOfTheOverbeing() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // As long as enchanted creature is green, it gets +1/+1 and has vigilance.
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                        new StaticBoostEffect(1, 1, Set.of(Keyword.VIGILANCE), GrantScope.ENCHANTED_CREATURE),
                        null
                ))
                // As long as enchanted creature is blue, it gets +1/+1 and has flying.
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE)),
                        new StaticBoostEffect(1, 1, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE),
                        null
                ));
    }
}
