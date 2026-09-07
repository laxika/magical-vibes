package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LazotepConvert;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "MOM", collectorNumber = "231")
public class InvasionOfAmonkhet extends Card {

    public InvasionOfAmonkhet() {
        setBackFaceCard(new LazotepConvert());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(3, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(3, MillRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
    }

    @Override
    public String getBackFaceClassName() {
        return "LazotepConvert";
    }
}
