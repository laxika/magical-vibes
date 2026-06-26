package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "XLN", collectorNumber = "75")
public class ShaperApprentice extends Card {

    public ShaperApprentice() {
        addEffect(EffectSlot.STATIC, new ControlsAnotherPermanentConditionalEffect(
                new PermanentHasSubtypePredicate(CardSubtype.MERFOLK),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)
        ));
    }
}
