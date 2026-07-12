package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "180")
@CardRegistration(set = "8ED", collectorNumber = "185")
public class Enrage extends Card {

    public Enrage() {
        // Target creature gets +X/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(new XValue(), new Fixed(0)));
    }
}
