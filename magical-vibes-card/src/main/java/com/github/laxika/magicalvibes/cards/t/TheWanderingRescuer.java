package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

@CardRegistration(set = "DSK", collectorNumber = "41")
public class TheWanderingRescuer extends Card {

    public TheWanderingRescuer() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HEXPROOF, GrantScope.OWN_CREATURES, new PermanentIsTappedPredicate()));
    }
}
