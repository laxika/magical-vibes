package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentManaValueSum;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "742")
@CardRegistration(set = "CMM", collectorNumber = "772")
public class NyxbornBehemoth extends Card {

    public NyxbornBehemoth() {
        PermanentAllOfPredicate noncreatureEnchantments = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())
        ));
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentManaValueSum(noncreatureEnchantments, CountScope.CONTROLLER)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsEnchantmentPredicate(), "another enchantment"),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)
                ),
                "{1}{G}, Sacrifice another enchantment: This creature gains indestructible until end of turn."
        ));
    }
}
