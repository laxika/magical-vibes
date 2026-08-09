package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForChosenSubtypeSpellsEffect;

@CardRegistration(set = "UDS", collectorNumber = "142")
public class UrzasIncubator extends Card {

    public UrzasIncubator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new ReduceCastCostForChosenSubtypeSpellsEffect(2));
    }
}
