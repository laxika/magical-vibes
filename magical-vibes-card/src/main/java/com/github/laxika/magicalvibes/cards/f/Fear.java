package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "162")
@CardRegistration(set = "4ED", collectorNumber = "137")
@CardRegistration(set = "10E", collectorNumber = "142")
@CardRegistration(set = "9ED", collectorNumber = "129")
@CardRegistration(set = "8ED", collectorNumber = "134")
@CardRegistration(set = "7ED", collectorNumber = "135")
@CardRegistration(set = "6ED", collectorNumber = "129")
@CardRegistration(set = "ICE", collectorNumber = "124")
public class Fear extends Card {

    public Fear() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FEAR, GrantScope.ENCHANTED_CREATURE));
    }
}
