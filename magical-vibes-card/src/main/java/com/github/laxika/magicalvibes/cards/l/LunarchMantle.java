package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "31")
public class LunarchMantle extends Card {

    public LunarchMantle() {
        // Enchant creature — enchanted creature gets +2/+2 and has
        // "{1}, Sacrifice a permanent: This creature gains flying until end of turn."
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{1}",
                                List.of(
                                        new SacrificePermanentCost(
                                                new PermanentTruePredicate(),
                                                "Sacrifice a permanent",
                                                false),
                                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)
                                ),
                                "{1}, Sacrifice a permanent: This creature gains flying until end of turn."
                        ),
                        GrantScope.ENCHANTED_CREATURE
                ));
    }
}
