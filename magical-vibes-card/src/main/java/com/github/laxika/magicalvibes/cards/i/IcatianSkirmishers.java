package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "FEM", collectorNumber = "14")
@CardRegistration(set = "FEM", collectorNumber = "161")
public class IcatianSkirmishers extends Card {

    public IcatianSkirmishers() {
        addEffect(EffectSlot.ON_ATTACK,
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.BANDED_WITH_SELF));
    }
}
