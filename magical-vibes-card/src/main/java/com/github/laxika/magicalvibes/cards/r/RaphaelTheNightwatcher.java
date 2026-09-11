package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "TMT", collectorNumber = "103")
@CardRegistration(set = "TMT", collectorNumber = "213")
@CardRegistration(set = "TMT", collectorNumber = "287")
@CardRegistration(set = "TMT", collectorNumber = "297")
public class RaphaelTheNightwatcher extends Card {

    public RaphaelTheNightwatcher() {
        addSneak("{1}{R}{R}");
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.DOUBLE_STRIKE, GrantScope.ALL_OWN_CREATURES, new PermanentIsAttackingPredicate()));
    }
}
