package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "BFZ", collectorNumber = "115")
public class KalastriaNightwatch extends Card {

    public KalastriaNightwatch() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
