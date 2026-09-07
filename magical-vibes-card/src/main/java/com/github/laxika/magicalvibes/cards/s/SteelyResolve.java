package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "286")
public class SteelyResolve extends Card {

    public SteelyResolve() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.SHROUD,
                GrantScope.ALL_CREATURES,
                new PermanentHasSourceChosenSubtypePredicate()));
    }
}
