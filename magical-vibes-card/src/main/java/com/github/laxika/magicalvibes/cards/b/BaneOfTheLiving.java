package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "LGN", collectorNumber = "60")
public class BaneOfTheLiving extends Card {

    public BaneOfTheLiving() {
        addMorph("{X}{B}{B}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new BoostAllCreaturesEffect(
                new Scaled(new XValue(), -1), new Scaled(new XValue(), -1)));
    }
}
