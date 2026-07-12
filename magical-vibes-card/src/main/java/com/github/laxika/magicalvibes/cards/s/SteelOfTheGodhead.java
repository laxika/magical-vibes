package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "150")
public class SteelOfTheGodhead extends Card {

    public SteelOfTheGodhead() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // As long as enchanted creature is white, it gets +1/+1 and has lifelink.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, Set.of(Keyword.LIFELINK), GrantScope.ENCHANTED_CREATURE,
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE))))
                // As long as enchanted creature is blue, it gets +1/+1 and can't be blocked.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, GrantScope.ENCHANTED_CREATURE,
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE))))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE)),
                        new CantBeBlockedEffect(),
                        null));
    }
}
