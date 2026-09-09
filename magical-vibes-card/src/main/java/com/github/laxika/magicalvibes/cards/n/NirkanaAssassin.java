package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "BFZ", collectorNumber = "118")
public class NirkanaAssassin extends Card {

    public NirkanaAssassin() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF));
    }
}
