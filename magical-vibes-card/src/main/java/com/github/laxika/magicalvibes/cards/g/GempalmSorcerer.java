package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "39")
public class GempalmSorcerer extends Card {

    public GempalmSorcerer() {
        addEffect(EffectSlot.ON_SELF_CYCLED,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.WIZARD)));
        addCycling("{2}{U}");
    }
}
