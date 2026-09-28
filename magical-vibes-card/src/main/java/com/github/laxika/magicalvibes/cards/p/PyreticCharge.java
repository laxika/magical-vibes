package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;

@CardRegistration(set = "OTC", collectorNumber = "29")
@CardRegistration(set = "OTC", collectorNumber = "65")
public class PyreticCharge extends Card {

    public PyreticCharge() {
        addEffect(EffectSlot.SPELL, new DiscardOwnHandThenDrawEffect(new Fixed(4)));
        addEffect(EffectSlot.SPELL,
                new BoostAllOwnCreaturesEffect(new EventValue(), new Fixed(0)));
    }
}
