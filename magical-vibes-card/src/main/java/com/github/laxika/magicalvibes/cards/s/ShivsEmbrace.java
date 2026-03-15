package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "156")
public class ShivsEmbrace extends Card {

    public ShivsEmbrace() {
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{R}",
                        List.of(new BoostSelfEffect(1, 0)),
                        "{R}: This creature gets +1/+0 until end of turn."
                ),
                GrantScope.ENCHANTED_CREATURE
        ));
    }
}
