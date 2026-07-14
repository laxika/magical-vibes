package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "146")
public class ScourgeOfTheNobilis extends Card {

    public ScourgeOfTheNobilis() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // As long as enchanted creature is red, it gets +1/+1...
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.RED)),
                        new StaticBoostEffect(1, 1, Set.of(), GrantScope.ENCHANTED_CREATURE),
                        null
                ))
                // ...and has "{R/W}: This creature gets +1/+0 until end of turn."
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.RED)),
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        false,
                                        "{R/W}",
                                        List.of(new BoostSelfEffect(1, 0)),
                                        "{R/W}: This creature gets +1/+0 until end of turn."
                                ),
                                GrantScope.ENCHANTED_CREATURE
                        ),
                        null
                ))
                // As long as enchanted creature is white, it gets +1/+1 and has lifelink.
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        new StaticBoostEffect(1, 1, Set.of(Keyword.LIFELINK), GrantScope.ENCHANTED_CREATURE),
                        null
                ));
    }
}
