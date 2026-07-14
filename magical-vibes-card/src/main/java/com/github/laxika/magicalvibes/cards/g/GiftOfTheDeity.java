package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "122")
public class GiftOfTheDeity extends Card {

    public GiftOfTheDeity() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        // As long as enchanted creature is black, it gets +1/+1 and has deathtouch.
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                new StaticBoostEffect(1, 1, Set.of(Keyword.DEATHTOUCH), GrantScope.ENCHANTED_CREATURE),
                null
        ));

        // As long as enchanted creature is green, it gets +1/+1 and all creatures able to block it do so.
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE),
                null
        ));
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                new MustBeBlockedByAllCreaturesEffect(),
                null
        ));
    }
}
