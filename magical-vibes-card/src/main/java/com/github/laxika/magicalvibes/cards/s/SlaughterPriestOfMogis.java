package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "227")
public class SlaughterPriestOfMogis extends Card {

    public SlaughterPriestOfMogis() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, new BoostSelfEffect(2, 0));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificePermanentCost(new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )), "another creature or an enchantment"),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)
                ),
                "{2}, Sacrifice another creature or an enchantment: This creature gains first strike until end of turn."
        ));
    }
}
