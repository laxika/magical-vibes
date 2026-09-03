package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesOfChosenSubtypeEffect;

@CardRegistration(set = "ONS", collectorNumber = "294")
public class TribalUnity extends Card {

    public TribalUnity() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesOfChosenSubtypeEffect(new XValue(), new XValue()));
    }
}
