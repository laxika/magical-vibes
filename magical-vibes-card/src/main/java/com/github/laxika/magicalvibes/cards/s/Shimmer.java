package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;

@CardRegistration(set = "MIR", collectorNumber = "92")
public class Shimmer extends Card {

    public Shimmer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseBasicLandTypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.PHASING, GrantScope.ALL_LANDS, new PermanentHasSourceChosenSubtypePredicate()));
    }
}
