package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ECL", collectorNumber = "101")
public class DreamSeizer extends Card {

    public DreamSeizer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new BlightEffect(1, new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT)),
                "Blight 1?"));
    }
}
