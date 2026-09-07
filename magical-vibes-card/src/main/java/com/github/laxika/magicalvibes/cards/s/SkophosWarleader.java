package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "154")
public class SkophosWarleader extends Card {

    public SkophosWarleader() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )), "another creature or an enchantment"),
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)
                ),
                "{R}, Sacrifice another creature or an enchantment: This creature gets +1/+0 and gains menace until end of turn."
        ));
    }
}
