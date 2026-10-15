package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "5DN", collectorNumber = "87")
public class FangrenPathcutter extends Card {

    public FangrenPathcutter() {
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(
                Keyword.TRAMPLE, GrantScope.ALL_CREATURES, new PermanentIsAttackingPredicate()
        ));
    }
}
