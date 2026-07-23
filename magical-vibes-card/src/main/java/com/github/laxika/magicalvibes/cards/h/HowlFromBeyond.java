package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "142")
@CardRegistration(set = "6ED", collectorNumber = "138")
@CardRegistration(set = "4ED", collectorNumber = "141")
@CardRegistration(set = "5ED", collectorNumber = "168")
@CardRegistration(set = "ICE", collectorNumber = "132")
public class HowlFromBeyond extends Card {

    public HowlFromBeyond() {
        // Target creature gets +X/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(new XValue(), new Fixed(0)));
    }
}
