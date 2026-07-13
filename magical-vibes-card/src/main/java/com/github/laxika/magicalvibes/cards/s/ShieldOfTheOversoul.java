package com.github.laxika.magicalvibes.cards.s;

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

@CardRegistration(set = "SHM", collectorNumber = "242")
public class ShieldOfTheOversoul extends Card {

    public ShieldOfTheOversoul() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // As long as enchanted creature is green, it gets +1/+1 and has indestructible.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, Set.of(Keyword.INDESTRUCTIBLE), GrantScope.ENCHANTED_CREATURE,
                        new PermanentColorInPredicate(Set.of(CardColor.GREEN))))
                // As long as enchanted creature is white, it gets +1/+1 and has flying.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE,
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
    }
}
