package com.github.laxika.magicalvibes.cards.c;

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
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "99")
public class CloutOfTheDominus extends Card {

    public CloutOfTheDominus() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // As long as enchanted creature is blue, it gets +1/+1 and has shroud.
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE)),
                        new StaticBoostEffect(1, 1, Set.of(Keyword.SHROUD), GrantScope.ENCHANTED_CREATURE),
                        null
                ))
                // As long as enchanted creature is red, it gets +1/+1 and has haste.
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.RED)),
                        new StaticBoostEffect(1, 1, Set.of(Keyword.HASTE), GrantScope.ENCHANTED_CREATURE),
                        null
                ));
    }
}
