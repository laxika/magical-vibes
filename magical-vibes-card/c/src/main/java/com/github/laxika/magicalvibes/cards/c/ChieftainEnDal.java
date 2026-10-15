package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "NEM", collectorNumber = "4")
public class ChieftainEnDal extends Card {

    public ChieftainEnDal() {
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(
                Keyword.FIRST_STRIKE,
                GrantScope.ALL_CREATURES,
                new PermanentIsAttackingPredicate()
        ));
    }
}
