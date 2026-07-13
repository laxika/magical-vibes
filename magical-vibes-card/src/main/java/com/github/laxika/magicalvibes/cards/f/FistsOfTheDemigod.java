package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "187")
public class FistsOfTheDemigod extends Card {

    public FistsOfTheDemigod() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // As long as enchanted creature is black, it gets +1/+1 and has wither.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, Set.of(Keyword.WITHER), GrantScope.ENCHANTED_CREATURE,
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK))))
                // As long as enchanted creature is red, it gets +1/+1 and has first strike.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, Set.of(Keyword.FIRST_STRIKE), GrantScope.ENCHANTED_CREATURE,
                        new PermanentColorInPredicate(Set.of(CardColor.RED))));
    }
}
