package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "203")
@CardRegistration(set = "9ED", collectorNumber = "186")
public class FlowstoneSlide extends Card {

    public FlowstoneSlide() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(
                new XValue(), new Scaled(new XValue(), -1)));
    }
}
