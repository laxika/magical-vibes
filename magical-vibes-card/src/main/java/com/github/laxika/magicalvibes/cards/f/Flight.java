package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "4ED", collectorNumber = "72")
@CardRegistration(set = "9ED", collectorNumber = "79")
@CardRegistration(set = "6ED", collectorNumber = "68")
@CardRegistration(set = "8ED", collectorNumber = "80")
@CardRegistration(set = "7ED", collectorNumber = "75")
@CardRegistration(set = "5ED", collectorNumber = "86")
public class Flight extends Card {

    public Flight() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));
    }
}
