package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCreaturesWithDifferentManaValuesDestroyEffect;

@CardRegistration(set = "DSC", collectorNumber = "356")
public class RunningIsUseless extends Card {

    public RunningIsUseless() {
        addEffect(EffectSlot.SPELL, new ChooseCreaturesWithDifferentManaValuesDestroyEffect());
    }
}
