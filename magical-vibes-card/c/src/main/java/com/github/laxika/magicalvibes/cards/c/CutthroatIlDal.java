package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "FUT", collectorNumber = "64")
@CardRegistration(set = "TSR", collectorNumber = "105")
public class CutthroatIlDal extends Card {

    public CutthroatIlDal() {
        addStaticEffect(new ConditionalEffect(
                new ControllerHandEmpty(), new GrantKeywordEffect(Keyword.SHADOW, GrantScope.SELF)));
    }
}
