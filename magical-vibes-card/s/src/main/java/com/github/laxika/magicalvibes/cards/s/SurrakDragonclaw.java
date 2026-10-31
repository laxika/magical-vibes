package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCreatureSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "KTK", collectorNumber = "206")
@CardRegistration(set = "DMC", collectorNumber = "169")
public class SurrakDragonclaw extends Card {

    public SurrakDragonclaw() {
        addStaticEffect(new CantBeCounteredEffect());
        addStaticEffect(new ControllerCreatureSpellsCantBeCounteredEffect());
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
    }
}
